package com.kuss.krude.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.kuss.krude.interfaces.AppExtensionGroup
import com.kuss.krude.interfaces.AppExtensionSingle
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import timber.log.Timber
import java.io.File

object ExtensionHelper {
    const val EXTENSIONS_REPO =
        "https://api.github.com/repos/kusstar/krude-extensions/contents/extensions"
    val FALLBACK_REPO = EXTENSIONS_REPO.replace("api.github.com", "github-api-proxy.deno.dev")

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
                context.startActivity(intent)
            }
            ExtensionType.ACTION -> {
                val intent = Intent(extension.uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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
            intent.setComponent(ComponentName(data.packageField, data.classField))
            if (data.extra != null) {
                data.extra.asMap().forEach {
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
            if (data.flags != null) {
                intent.setFlags(data.flags.toInt())
            }
            if (data.action != null) {
                intent.setAction(data.action)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchExtensionsFromRepo(
        context: Context,
        repoUrl: String
    ): Pair<Exception?, List<String>?> {
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
                val extensionUrls: MutableList<String> = mutableListOf()
                try {
                    gson.fromJson(body, JsonArray::class.java)?.forEach { data ->
                        val name = data.asJsonObject.get("name").asString
                        Timber.d("fetchExtensionsFromRepo: extension = $name")
                        val url = data.asJsonObject.get("download_url").asString
                        extensionUrls.add(url)
                    }
                    Timber.d("fetchExtensionsFromRepo: extensionUrls = $extensionUrls")
                    return Pair(null, extensionUrls)
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
                if (url.startsWith("https://raw.githubusercontent.com")) {
                    Timber.d("fetchExtension: fallback to GH_RAW_PROXY")
                    fetchExtension(context, "$GH_RAW_PROXY/$url", onResult)
                }
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

}
