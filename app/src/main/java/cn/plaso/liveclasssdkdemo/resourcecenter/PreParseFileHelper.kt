package cn.plaso.liveclasssdkdemo.resourcecenter

import android.util.Log
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 预解析文件 URL 拉取（对齐 iOS CloudDiskPreParseHelper）。
 */
object PreParseFileHelper {
    private const val TAG = "PreParseFileHelper"
    private const val PARSED_HOST = "120.55.3.51"
    private const val PARSED_PORT = 3000
    private const val API_BASE = "api/files"

    /** SDK 图片模式拉原始文档签名 URL 的 suffix（与窗口模式页资源 suffix 区分） */
    const val PICTURE_MODE_SOURCE_SUFFIX = "?"

    private val httpClient by lazy { OkHttpClient.Builder().build() }
    private val gson = Gson()

    private data class ParsedUrlResp(val success: Boolean = false, val url: String? = null)

    fun extractPreParseFileId(info: Any?): String? {
        if (info is Map<*, *>) {
            val id = info["id"] as? String
            if (!id.isNullOrBlank()) return id
        }
        if (info is Iterable<*>) {
            val list = info.toList()
            if (list.size >= 2) {
                extractPreParseFileId(list[1])?.let { return it }
            }
        }
        if (info is Array<*>) {
            if (info.size >= 2) {
                extractPreParseFileId(info[1])?.let { return it }
            }
        }
        val text = info?.toString().orEmpty()
        val match = Regex("""id\s*=\s*([0-9a-fA-F\-]{36}|[0-9a-zA-Z\-_]+)""").find(text)
        return match?.groupValues?.get(1)
    }

    /** 图片模式插入前拉取带签名的原始文档 URL（对齐 YXT getSignedPath） */
    fun fetchSignedOriginalDocumentUrl(fileId: String, callback: (String?) -> Unit) {
        if (fileId.isBlank()) {
            callback(null)
            return
        }
        val httpUrl = HttpUrl.Builder()
            .scheme("http")
            .host(PARSED_HOST)
            .port(PARSED_PORT)
            .addPathSegments(API_BASE)
            .addPathSegment(fileId)
            .addPathSegment("parsed-url")
            .addQueryParameter("suffix", PICTURE_MODE_SOURCE_SUFFIX)
            .build()
        Log.d(TAG, "fetchSignedOriginalDocumentUrl: $httpUrl")
        requestParsedUrl(httpUrl, callback)
    }

    /** getPreParseFileName：窗口模式页资源；suffix=? 为 SDK 图片模式兜底 */
    fun fetchPreParseFileName(info: Any?, suffix: String?, callback: (String?) -> Unit) {
        if (suffix == PICTURE_MODE_SOURCE_SUFFIX) {
            val id = extractPreParseFileId(info)
            if (id.isNullOrBlank()) {
                callback(null)
                return
            }
            fetchSignedOriginalDocumentUrl(id, callback)
            return
        }
        val id = extractPreParseFileId(info) ?: run {
            callback(null)
            return
        }
        val fileName = "_i$suffix"
        val httpUrl = HttpUrl.Builder()
            .scheme("http")
            .host(PARSED_HOST)
            .port(PARSED_PORT)
            .addPathSegments(API_BASE)
            .addPathSegment(id)
            .addPathSegment("parsed-url")
            .addQueryParameter("suffix", fileName)
            .build()
        Log.d(TAG, "fetchPreParseFileName: $httpUrl")
        requestParsedUrl(httpUrl, callback)
    }

    private fun requestParsedUrl(httpUrl: HttpUrl, callback: (String?) -> Unit) {
        val request = Request.Builder()
            .get()
            .addHeader("Accept", "application/json")
            .url(httpUrl)
            .build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "requestParsedUrl error: $httpUrl", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    val body = resp.body?.string().orEmpty()
                    Log.d(TAG, "requestParsedUrl code=${resp.code}, body=$body")
                    val parsed = try {
                        gson.fromJson(body, ParsedUrlResp::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "requestParsedUrl parse error", e)
                        null
                    }
                    val realUrl = parsed?.takeIf { it.success }?.url
                    if (realUrl.isNullOrBlank()) {
                        Log.e(TAG, "requestParsedUrl failed: url is null or success=false")
                    }
                    callback(realUrl)
                }
            }
        })
    }
}
