package com.kuss.krude.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

data class BingResponse(
    val AS: BingASData
)

data class BingASData(
    val Query: String,
    val FullResults: Int,
    val Results: List<BingResult>
)

data class BingResult(
    val Type: String,
    val Suggests: List<BingSuggestion>
)

data class BingSuggestion(
    val Txt: String,
    val Type: String,
    val Sk: String,
    val HCS: Int
)

data class GptApp(
    val name: String,
    val `package`: String,
    val scheme: String
)

data class GptData(
    val search: String,
    val apps: List<GptApp>
)

const val MOCK_DATA =
    """[{"search":"今天吃什么","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]},{"search":"美食推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"},{"name":"小红书","package":"com.xingin.xhs","scheme":"xhsdiscover://search/result?keyword=queryplaceholder"},{"name":"抖音","package":"com.ss.android.ugc.aweme","scheme":"snssdk1128://search/result?keyword=queryplaceholder"}]},{"search":"餐厅推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]},{"search":"外卖推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]},{"search":"健康饮食","apps":[{"name":"小红书","package":"com.xingin.xhs","scheme":"xhsdiscover://search/result?keyword=queryplaceholder"},{"name":"抖音","package":"com.ss.android.ugc.aweme","scheme":"snssdk1128://search/result?keyword=queryplaceholder"}]},{"search":"减肥食谱","apps":[{"name":"小红书","package":"com.xingin.xhs","scheme":"xhsdiscover://search/result?keyword=queryplaceholder"},{"name":"抖音","package":"com.ss.android.ugc.aweme","scheme":"snssdk1128://search/result?keyword=queryplaceholder"}]},{"search":"素食推荐","apps":[{"name":"小红书","package":"com.xingin.xhs","scheme":"xhsdiscover://search/result?keyword=queryplaceholder"},{"name":"抖音","package":"com.ss.android.ugc.aweme","scheme":"snssdk1128://search/result?keyword=queryplaceholder"}]},{"search":"快餐推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]},{"search":"夜宵推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]},{"search":"甜品推荐","apps":[{"name":"美团","package":"com.sankuai.meituan","scheme":"imeituan://www.meituan.com/search?q=queryplaceholder"},{"name":"饿了么","package":"me.ele","scheme":"eleme://search?keyword=queryplaceholder"}]}]"""

class SearchHelper {

    companion object {
        val client = OkHttpClient()

        fun queryBing(query: String, onResult: (result: List<String>) -> Unit) {
            // 创建 OkHttpClient 实例

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
                            Log.d(
                                "SearchHelper",
                                "queryBing failed, err=${err.stackTraceToString()}"
                            )
                            onResult(listOf())
                        }
                    } else {
                        Log.d("SearchHelper", "queryBing failed")
                        onResult(listOf())
                    }
                }
            })
        }

        const val QUERY_GPT_TAG = "queryGpt"

        fun getDefaultGptData(query: String): GptData {
            return GptData(
                query,
                listOf(
                    GptApp(
                        "Bing",
                        "krude.browser.search",
                        "https://cn.bing.com/search?q=queryplaceholder"
                    ),
                    GptApp(
                        "Google",
                        "krude.browser.search",
                        "https://www.google.com/search?q=queryplaceholder"
                    ),
                    GptApp(
                        "百度",
                        "krude.browser.search",
                        "https://www.baidu.com/s?wd=queryplaceholder"
                    ),
                    GptApp(
                        "抖音",
                        "krude.browser.search",
                        "https://www.douyin.com/search/queryplaceholder"
                    ),
                    GptApp(
                        "头条",
                        "krude.browser.search",
                        "https://m.toutiao.com/search?q=queryplaceholder"
                    ),
                    GptApp(
                        "GitHub",
                        "krude.browser.search",
                        "https://github.com/search?q=queryplaceholder"
                    ),
                    GptApp(
                        "Bilibili",
                        "krude.browser.search",
                        "https://search.bilibili.com/all?keyword=queryplaceholder"
                    ),
                    GptApp(
                        "知乎",
                        "krude.browser.search",
                        "https://www.zhihu.com/search?q=queryplaceholder"
                    ),
                )
            )
        }

        fun cancelCallWithTag(tag: String) {
            client.dispatcher.queuedCalls().forEach {
                if (it.request().tag() == tag) {
                    it.cancel()
                }
            }
            client.dispatcher.runningCalls().forEach {
                if (it.request().tag() == tag) {
                    it.cancel()
                }
            }
        }

        fun queryGpt(query: String, onResult: (result: List<GptData>) -> Unit) {
            cancelCallWithTag(QUERY_GPT_TAG)
            // 构建请求 URL
            val url = "https://krude-search.deno.dev/api/search?q=$query"

            // 创建请求对象
            val request = Request.Builder()
                .tag(QUERY_GPT_TAG)
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
                            val typeToken = object : TypeToken<List<GptData>>() {}.type
                            val obj = gson.fromJson<List<GptData>>(responseBody, typeToken)
                            onResult(obj)
                        } catch (err: Exception) {
                            Log.d(
                                "SearchHelper",
                                "queryBing failed, err=${err.stackTraceToString()}"
                            )
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