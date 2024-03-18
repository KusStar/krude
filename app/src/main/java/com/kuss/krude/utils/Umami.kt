package com.kuss.krude.utils

import android.os.Build
import android.util.Log
import com.kuss.krude.BuildConfig
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Data class to represent the tracked properties similarly to the TypeScript type.
 * Nullable types are used to represent optional properties.
 */
data class TrackedProperties(
    val website: String,
    val hostname: String?,
    val language: String?,
    val referrer: String?,
    val screen: String?,
    val title: String?,
    val url: String?,
    val page: String?,
    val app: String?,
    val os: String?,
    val device: String?
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["website"] = website
        if (hostname != null) map["hostname"] = hostname
        if (language != null) map["language"] = language
        if (referrer != null) map["referrer"] = referrer
        if (screen != null) map["screen"] = screen
        if (title != null) map["title"] = title
        if (url != null) map["url"] = url
        if (page != null) map["page"] = page
        if (app != null) map["app"] = app
        if (os != null) map["os"] = os
        if (device != null) map["device"] = device
        return map
    }
}

/**
 * Singleton object to represent the tracking functionality.
 * This is analogous to the functions and variables in the TypeScript example.
 */
object Umami {
    private const val WEBSITE_ID = "krude"
    private const val HOST_URL = "https://umami.uselessthing.top"

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase(Locale.getDefault())
                .startsWith(manufacturer.lowercase(Locale.getDefault()))
        ) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s.isNullOrEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            first.uppercaseChar().toString() + s.substring(1)
        }
    }

    private var payload = TrackedProperties(
        website = "",
        language = null,
        referrer = null,
        screen = null,
        title = null,
        page = null,
        hostname = null,
        url = null,
        app = "krude ${BuildConfig.VERSION_NAME}",
        os = "Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}",
        device = getDeviceName()
    )
    private var cache: String? = null
    private var endpoint: String = ""
    private val executor = Executors.newSingleThreadExecutor()

    private fun init(websiteId: String, hostUrl: String, extraPayload: TrackedProperties? = null) {
        payload = extraPayload?.let {
            payload.copy(
                website = websiteId,
                hostname = it.hostname ?: payload.hostname,
                language = it.language ?: payload.language,
                referrer = it.referrer ?: payload.referrer,
                screen = it.screen ?: payload.screen,
                title = it.title ?: payload.title,
                url = it.url ?: payload.url,
                page = it.page ?: payload.page,
                app = it.app ?: payload.app,
                os = it.os ?: payload.os,
                device = it.device ?: payload.device
            )
        } ?: payload.copy(website = websiteId)
        endpoint = "$hostUrl/api/send"
    }

    private fun send(extraPayload: TrackedProperties?, type: String = "event") {
        val headers = mutableMapOf("Content-Type" to "application/json")
        cache?.let { headers["x-umami-cache"] = it }

        val nextPayload = extraPayload?.let {
            payload.copy(
                hostname = it.hostname ?: payload.hostname,
                language = it.language,
                referrer = it.referrer,
                screen = it.screen,
                title = it.title,
                url = it.url ?: payload.url,
                page = it.page ?: payload.page,
                app = it.app ?: payload.app,
                os = it.os ?: payload.os,
                device = it.device ?: payload.device
            )
        } ?: payload

        val body = JSONObject(mapOf("type" to type, "payload" to nextPayload.toMap()))

        executor.execute {
            val urlObject = URL(endpoint)
            val connection = urlObject.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.useCaches = false
            connection.connectTimeout = 5000
            try {
                headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.writeBytes(body.toString())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode

                if (responseCode == HTTP_OK) {
                    val data = connection.inputStream.bufferedReader().readText()
                    cache = data
                    Log.d(TAG, cache!!)
                } else {
                    val errorText = connection.errorStream?.bufferedReader()?.readText()
                    Log.e(TAG, "Error: $responseCode, ${connection.responseMessage}, $errorText")
                }

            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
            } finally {
                connection.disconnect()
            }
        }
    }

    fun trackInit(payload: TrackedProperties? = null) {
        init(WEBSITE_ID, HOST_URL, null)

        send(payload)
    }

    fun trackEvent(payload: TrackedProperties? = null) {
        send(payload)
    }
}