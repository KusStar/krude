package com.kuss.krude.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.kuss.krude.interfaces.AppExtensionGroup
import com.kuss.krude.interfaces.AppExtensionSingle
import com.kuss.krude.interfaces.Extension
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import timber.log.Timber
import java.io.File

object ExtensionHelper {
    val DEFAULT_EXTENSIONS_RULES = listOf(
        "https://gist.githubusercontent.com/KusStar/7eacbec872b85ed12d5a72bc1113ddbe/raw/11026021d965385214876a0670236cbcec0661ab/krude-apps-extensions.json",
        "https://gist.githubusercontent.com/KusStar/7eacbec872b85ed12d5a72bc1113ddbe/raw/11026021d965385214876a0670236cbcec0661ab/krude-setting-extensions.json"
    )

    private var client: OkHttpClient? = null

    private fun initClient(context: Context) {
        client = OkHttpClient.Builder()
            .followRedirects(true)
            .cache(Cache(
                directory = File(context.cacheDir, "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MiB
            ))
            .build()
    }

    fun launchExtensionIntent(context: Context, extension: Extension) {
        try {
            val intent = Intent()
            val data = extension.data!!
            intent.setComponent(ComponentName(data.packageField, data.classField))
            val extra = data.extra
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
            intent.setFlags(data.flags.toInt())
            intent.setAction(data.action)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchExtension(context: Context, url: String, onResult: (appExtensionGroup: AppExtensionGroup?) -> Unit) {
        if (client == null) {
            initClient(context)
        }

        val request = Request.Builder()
            .url(url)
            .build()

        client!!.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
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
                    } catch(e: Exception) {
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
                    onResult(appExtensionGroup)
                }
            }
        })
    }

}
