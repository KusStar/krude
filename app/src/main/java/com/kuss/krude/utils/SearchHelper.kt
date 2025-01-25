package com.kuss.krude.utils

import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

data class BingResponse(
    val AS: ASData
)

data class ASData(
    val Query: String,
    val FullResults: Int,
    val Results: List<Result>
)

data class Result(
    val Type: String,
    val Suggests: List<Suggestion>
)

data class Suggestion(
    val Txt: String,
    val Type: String,
    val Sk: String,
    val HCS: Int
)

class SearchHelper {
    companion object {
        fun queryBing(query: String, onResult: (result: List<String>) -> Unit) {
            // 创建 OkHttpClient 实例
            val client = OkHttpClient()

            // 构建请求 URL
            val url = "http://sg1.api.bing.com/qsonhs.aspx?type=json&q=$query"

            // 创建请求对象
            val request = Request.Builder()
                .url(url)
                .build()

            // 发起异步请求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // 请求失败处理
                    e.printStackTrace()
                    println("请求失败: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    // 请求成功处理
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            val gson = Gson()
                            val obj = gson.fromJson(responseBody, BingResponse::class.java)
                            val result = mutableListOf<String>()
                            obj.AS.Results.forEach {
                                it.Suggests.forEach { it2 ->
                                    result.add(it2.Txt)
                                }
                            }
                            onResult(result)
                        } catch (err: Exception) {
                            Log.d("SearchHelper", "queryBing failed, err=${err.stackTraceToString()}")
                            onResult(listOf())
                        }
                    } else {
                        Log.d("SearchHelper", "queryBing failed")
                        onResult(listOf())
                    }
                }
            })
        }
    }
}