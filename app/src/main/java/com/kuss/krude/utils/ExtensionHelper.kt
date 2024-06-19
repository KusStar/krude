package com.kuss.krude.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kuss.krude.interfaces.AppExtensionGroup
import com.kuss.krude.interfaces.AppExtensionSingle
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.interfaces.I18NExtension
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import timber.log.Timber
import java.io.File

data class ExtensionRepo(val name: String, val url: String)

object ExtensionHelper {
    const val EXTENSIONS_REPO =
        "https://api.github.com/repos/kusstar/krude-extensions/contents/extensions"
    private val FALLBACK_REPO = EXTENSIONS_REPO.replace("api.github.com", "github-api-proxy.deno.dev")

    const val GH_RAW_PROXY = "https://mirror.ghproxy.com"

    private var client: OkHttpClient? = null

    private fun initClient(context: Context) {
        client = OkHttpClient.Builder()
            .followRedirects(true)
            .cache(
                Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    fun launchExtension(context: Context, extension: Extension) {
        when (extension.type) {
            ExtensionType.SCHEME -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(extension.uri)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                ActivityHelper.startIntentWithTransition(context, intent)
            }
            ExtensionType.ACTION -> {
                val intent = Intent(extension.uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                extension.data?.let { data ->
                    intentPutExtra(intent, data.extra)
                    if (data.flags != null) {
                        intent.setFlags(data.flags.toInt())
                    }
                }
                ActivityHelper.startIntentWithTransition(context, intent)
            }
            ExtensionType.INTENT -> launchExtensionIntent(
                context,
                extension
            )
        }
    }

    private fun launchExtensionIntent(context: Context, extension: Extension) {
        try {
            Timber.d("launchExtensionIntent: extension = $extension")
            val intent = Intent()
            val data = extension.data!!
            if (data.packageField != null && data.classField != null) {
                intent.setComponent(ComponentName(data.packageField, data.classField))
            }
            if (data.extra != null) {
                intentPutExtra(intent, data.extra)
            }
            if (data.flags != null) {
                intent.setFlags(data.flags.toInt())
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (data.action != null) {
                intent.setAction(data.action)
            }
            ActivityHelper.startIntentWithTransition(context, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun intentPutExtra(intent: Intent, dataExtra: JsonObject?) {
        dataExtra?.let { extra ->
            extra.asMap().forEach {
                if (it.value.isJsonPrimitive) {
                    val primitive = it.value.asJsonPrimitive
                    when {
                        primitive.isString -> intent.putExtra(it.key, primitive.asString)
                        primitive.isNumber -> intent.putExtra(it.key, primitive.asNumber.toInt())
                        primitive.isBoolean -> intent.putExtra(it.key, primitive.asBoolean)
                        else -> println("The value of '$it.key' is neither a String nor a Number.")
                    }
                }
            }
        }
    }

    fun fetchExtensionsFromRepo(
        context: Context,
        repoUrl: String
    ): Pair<Exception?, List<ExtensionRepo>?> {
        if (client == null) {
            initClient(context)
        }

        val request = Request.Builder()
            .url(repoUrl)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()

        try {
            val response = client!!.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code $response")
                val body = it.body?.string()
                val gson = Gson()
                val extensionRepos: MutableList<ExtensionRepo> = mutableListOf()
                try {
                    gson.fromJson(body, JsonArray::class.java)?.forEach { data ->
                        val name = data.asJsonObject.get("name").asString
                        Timber.d("fetchExtensionsFromRepo: extension = $name")
                        val url = data.asJsonObject.get("download_url").asString
                        extensionRepos.add(ExtensionRepo(name, url))
                    }
                    Timber.d("fetchExtensionsFromRepo: extensionUrls = $extensionRepos")
                    return Pair(null, extensionRepos)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return Pair(e, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (repoUrl.startsWith("https://api.github.com")) {
                Timber.d("fetchExtensionsFromRepo: fallback to FALLBACK_REPO")
                return fetchExtensionsFromRepo(context, FALLBACK_REPO)
            } else {
                return Pair(e, null)
            }
        }
    }

    fun fetchExtension(
        context: Context,
        url: String,
    ): AppExtensionGroup? {
        Timber.d("fetchExtension url = $url")
        if (client == null) {
            initClient(context)
        }

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client!!.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) throw IOException("Unexpected code $response")

                val body = it.body?.string()
                val gson = Gson()
                return try {
                    val appExtensionGroup = gson.fromJson(body, AppExtensionGroup::class.java)
                    Timber.d("fetchExtension: group = ${appExtensionGroup.main}")
                    appExtensionGroup
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        val appExtensionSingle = gson.fromJson(body, AppExtensionSingle::class.java)
                        Timber.d("fetchExtension: single = ${appExtensionSingle.main}")
                        AppExtensionGroup(appExtensionSingle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (url.startsWith("https://raw.githubusercontent.com")) {
                Timber.d("fetchExtension: fallback to GH_RAW_PROXY")
                return fetchExtension(context, "$GH_RAW_PROXY/$url")
            }
            return null
        }
    }

    fun overwriteI18nExtension(target: Extension, from: I18NExtension) {
        from.name?.let { target.name = it }
        from.description?.let { target.description = it }
        from.keywords?.let { target.keywords = it }
        from.type?.let { target.type = it }
        from.uri?.let { target.uri = it }
        from.data?.let { target.data = it }
    }

}
