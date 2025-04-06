package com.kuss.krude.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
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
    const val EXTENSIONS_REPO = "https://kexts.uselessthing.top"
    private const val DENO_EXTENSIONS_REPO = "https://krude-extensions.deno.dev"
    private const val GITHUB_EXTENSIONS_REPO =
        "https://api.github.com/repos/kusstar/krude-extensions/contents/extensions"
    private val GITHUB_PROXY_EXTENSIONS_REPO = GITHUB_EXTENSIONS_REPO
        .replace("api.github.com", "github-api-proxy.deno.dev")

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

    private fun removedQueryParamsUrl(url: String?): String {
        val uri = Uri.parse(url)
        return uri.buildUpon().apply {
            if (uri.query.toString().contains("queryplaceholder")) {
                query(null)
            }
        }.toString()
    }

    fun launchExtension(context: Context, extension: Extension, isFreeformWindow: Boolean) {
        Timber.i("launchExtension: extension = $extension, type = ${extension.type}, isFreeformWindow = $isFreeformWindow")
        when (extension.type) {
            ExtensionType.SCHEME -> {
                val uri = removedQueryParamsUrl(extension.uri).toUri();
                Timber.i("launchExtension: scheme uri = $uri")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                ActivityHelper.startIntentWithTransition(context, intent, isFreeformWindow)
            }
            ExtensionType.ACTION -> {
                val uri = removedQueryParamsUrl(extension.uri)
                val intent = Intent(uri)
                Timber.i("launchExtension: action uri = $uri")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                extension.data?.let { data ->
                    intentPutExtra(intent, data.extra)
                    if (data.flags != null) {
                        intent.setFlags(data.flags.toInt())
                    }
                }
                ActivityHelper.startIntentWithTransition(context, intent, isFreeformWindow)
            }
            ExtensionType.INTENT -> launchExtensionIntent(
                context,
                extension,
                isFreeformWindow
            )
        }
    }

    private fun launchExtensionIntent(
        context: Context,
        extension: Extension,
        isFreeformWindow: Boolean
    ) {
        try {
            val intent = Intent()
            val data = extension.data!!
            Timber.i("launchExtensionIntent: extension = $extension, data = $data")
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
            ActivityHelper.startIntentWithTransition(context, intent, isFreeformWindow)
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
            if (repoUrl == EXTENSIONS_REPO) {
                Timber.d("fetchExtensionsFromRepo: fallback to $DENO_EXTENSIONS_REPO")
                return fetchExtensionsFromRepo(context, DENO_EXTENSIONS_REPO)
            }
            if (repoUrl == DENO_EXTENSIONS_REPO) {
                Timber.d("fetchExtensionsFromRepo: fallback to $GITHUB_EXTENSIONS_REPO")
                return fetchExtensionsFromRepo(context, GITHUB_EXTENSIONS_REPO)
            }
            if (repoUrl == GITHUB_EXTENSIONS_REPO) {
                Timber.d("fetchExtensionsFromRepo: fallback to $GITHUB_PROXY_EXTENSIONS_REPO")
                return fetchExtensionsFromRepo(context, GITHUB_PROXY_EXTENSIONS_REPO)
            } else {
                return Pair(e, null)
            }
        }
    }

    fun fetchExtension(
        context: Context,
        url: String,
        onResult: (appExtensionGroup: AppExtensionGroup?) -> Unit
    ) {
        Timber.d("fetchExtension url = $url")
        if (client == null) {
            initClient(context)
        }

        val request = Request.Builder()
            .url(url)
            .build()

        client!!.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                Timber.e("fetchExtension: cannot fetch extension from $url")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) throw IOException("Unexpected code $response")

                    val body = it.body?.string()
                    val gson = Gson()
                    val appExtensionGroup = try {
                        val appExtensionGroup = gson.fromJson(body, AppExtensionGroup::class.java)
                        Timber.d("fetchExtension: group = ${appExtensionGroup.main}")
                        appExtensionGroup
                    } catch (e: Exception) {
                        e.printStackTrace()
                        try {
                            val appExtensionSingle =
                                gson.fromJson(body, AppExtensionSingle::class.java)
                            Timber.d("fetchExtension: single = ${appExtensionSingle.main}")
                            AppExtensionGroup(appExtensionSingle)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    onResult(appExtensionGroup)
                }
            }
        })
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
