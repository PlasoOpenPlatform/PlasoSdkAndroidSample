package cn.plaso.liveclasssdkdemo

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import cn.plaso.liveclasssdkdemo.minilesson.LessonInfoWrap
import cn.plaso.liveclasssdkdemo.resourcecenter.CloudFilePickerActivity
import cn.plaso.upime.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList

class DemoApp : Application() {
    companion object {
        var upimeBoard: UpimeBoard? = null
        const val TAG = "DemoApp"
        lateinit var sp: SharedPreferences
        val GSON = Gson()
        lateinit var app: DemoApp
        lateinit var upime: StyleUpime
        var newPPt: Boolean = false

        // Parsed file API constants (keep identical behavior; extracted for reuse)
        private const val PARSED_HOST = "120.55.3.51"
        private const val PARSED_PORT = 3000
        private const val API_BASE = "api/files"
    }

    // Reuse a single OkHttpClient across requests
    private val httpClient by lazy { OkHttpClient.Builder().build() }

    lateinit var resProvider: IResourceProvider
    private data class ParsedUrlResp(val success: Boolean = false, val url: String? = null)
    override fun onCreate() {
        super.onCreate()
        app = this
        sp = getSharedPreferences("demo", MODE_PRIVATE)

        resProvider = object : IResourceProvider {
            override fun supportResourceCenter(): Boolean {
                return true
            }

            override fun showResourceCenter(checkAi: Boolean) {
                val intent = Intent(applicationContext, CloudFilePickerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            override fun dismissResourceCenter() {
            }

            override fun getExtFileName(info: Any?, resouceCallback: IResourceCallback?) {
                Log.i(TAG, "getExtFileName $info className ${info?.javaClass?.name}")
                // 这个info就是调用insertObject插入时候UpimeObject里面的info参数，回调回来的。
                if (info == null || info !is ArrayList<*>) {
                    Log.e(TAG, "getExtFileName info is null")
                    return
                }
                fun toMap(obj: Any?): Map<String, Any?>? {
                    if (obj is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        return obj as Map<String, Any?>
                    }
                    if (obj is Iterable<*>) {
                        val list = obj.toList()
                        if (list.size >= 2 && list[1] is Map<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            return list[1] as Map<String, Any?>
                        }
                    }
                    if (obj is Array<*>) {
                        if (obj.size >= 2 && obj[1] is Map<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            return obj[1] as Map<String, Any?>
                        }
                    }
                    val s = obj?.toString().orEmpty()
                    if (s.contains("path=") && s.contains("fileName=")) {
                        fun extract(key: String): String? {
                            val r = Regex("""$key\s*=\s*([^,}\]]+)""")
                            return r.find(s)?.groupValues?.get(1)?.trim()
                        }
                        return mapOf(
                            "host" to extract("host"),
                            "path" to extract("path"),
                            "fileName" to extract("fileName")
                        )
                    }
                    return null
                }

                if (info[0]?.equals("struct") == true) {
                    val map = toMap(info)
                    if (map == null) {
                        Log.e(TAG, "getExtFileName parse failed: info=$info")
                        return
                    }

                    val host = (map["host"] as? String).orEmpty()
                    val path = (map["path"] as? String).orEmpty()
                    val fileName = (map["fileName"] as? String) ?: (map["filename"] as? String) ?: ""

                    if (fileName.isBlank()) {
                        Log.e(TAG, "getExtFileName missing fileName in $map")
                        return
                    }

                    fun join(a: String, b: String): String {
                        return when {
                            a.isEmpty() -> b
                            a.endsWith("/") && b.startsWith("/") -> a + b.removePrefix("/")
                            !a.endsWith("/") && !b.startsWith("/") -> "$a/$b"
                            else -> a + b
                        }
                    }

                    var url = if (host.isNotBlank()) join(host, path) else path
                    url = join(url, fileName)

                    Log.d(TAG, "getExtFileName realUrl=$url")
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        resouceCallback?.onFilePath(url)
                    }
                }
            }

            override fun getPreParseFileName(
                info: Any?,
                suffix: String?,
                callback: IResourceCallback?
            ) {
                // 这个info就是调用insertObject插入时候UpimeObject里面的info参数，回调回来的。
                // 形如：[parsed, {id=xxxx}]
                Log.d(TAG, "getPreParseFileName $info , $suffix")

                fun extractId(value: Any?): String? {
                    val text = value?.toString().orEmpty()
                    val m = Regex("""id\s*=\s*([0-9a-fA-F\-]{36}|[0-9a-zA-Z\-_]+)""").find(text)
                    return m?.groupValues?.get(1)
                }

                val id = extractId(info)
                Log.d(TAG, "parsed id = $id")
                // /1.jpg
                val fileName = "_i$suffix"
                val httpUrl: okhttp3.HttpUrl = okhttp3.HttpUrl.Builder()
                    .scheme("http")
                    .host(PARSED_HOST)
                    .port(PARSED_PORT)
                    .addPathSegments(API_BASE)
                    .addPathSegment(id.toString())
                    .addPathSegment("parsed-url")
                    .addQueryParameter("suffix", fileName) // 会自动对 "/" 进行编码为 %2F
                    .build()
                Log.d(TAG, "getPreParseFileName httpPath: $httpUrl")

                val request = Request.Builder()
                    .get()
                    .addHeader("Accept", "application/json")
                    .url(httpUrl)
                    .build()
                httpClient.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        Log.e(TAG, "getPreParseFileName request error", e)
                    }
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        response.use { resp ->
                            val body = resp.body?.string().orEmpty()
                            Log.d(TAG, "getPreParseFileName http code=${resp.code}, body=$body")
                            val parsed = try {
                                GSON.fromJson(body, ParsedUrlResp::class.java)
                            } catch (e: Exception) {
                                Log.e(TAG, "getPreParseFileName parse error", e)
                                null
                            }
                            Log.d(TAG, "getPreParseFileName parsed=$parsed")
                            val realUrl = parsed?.takeIf { it.success }?.url
                            if (!realUrl.isNullOrBlank()) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Log.d(TAG, "getPreParseFileName realUrl=$realUrl")
                                    callback?.onFilePath(realUrl)
                                }
                            } else {
                                Log.e(TAG, "getPreParseFileName parse failed: url is null or success=false")
                            }
                        }
                    }
                })
            }

            override fun signQuery(queryMap: MutableMap<String, Any>?, cb: SignCallback?): Boolean {
                if (queryMap != null) {
                    queryMap["appId"] = Config.appId
                    queryMap["validTime"] = 120
                    queryMap["validBegin"] =  System.currentTimeMillis() / 1000
                    // TODO get it from server
                    // this is only for test
                    val signedQuery = SignHelper.sign(queryMap, Config.appKey)
                    cb?.onSignCompleted(signedQuery)
                } else {
                    cb?.onSignCompleted(null)
                }

                return true
            }

            override fun getWebviewUrl(type: Int, callback: IUrlCallback?) {
                callback?.onGetUrl("https://www.plaso.cn")
            }

        }
        upime = StyleUpime.create(this, "demo", resProvider)
    }

    fun saveLessonInfo(lesson: LessonInfoWrap) {
        val lessonInfoList = getMiniLessonList()

        var savedLesson=false
        lessonInfoList.forEach {
            if (it.lessonInfo.recordId == lesson.lessonInfo.recordId) {
                savedLesson = true
                it.lessonInfo = lesson.lessonInfo
                it.status = lesson.status
                return@forEach
            }
        }

        if (!savedLesson) {
            lessonInfoList.add(lesson)
        }

        sp.edit().putString("lessoninfo", GSON.toJson(lessonInfoList)).commit()
    }

    fun deleteLessonInfo(lesson: LessonInfoWrap) {
        val lessonInfoList = getMiniLessonList()
        var savedLesson:LessonInfoWrap? = null
        lessonInfoList.forEach {
            if (it.lessonInfo.recordId == lesson.lessonInfo.recordId) {
                savedLesson = it
                return@forEach
            }
        }
        savedLesson?.let {
            lessonInfoList.remove(it)
            sp.edit().putString("lessoninfo", GSON.toJson(lessonInfoList)).commit()
        }
    }

    fun getMiniLessonList(): MutableList<LessonInfoWrap> {
        val lessonInfoJson = sp.getString("lessoninfo", null) ?: return mutableListOf()
        return GSON.fromJson(
            lessonInfoJson,
            object : TypeToken<MutableList<LessonInfoWrap>>() {}.type
        )
    }

    fun getLessonInfo(recordId:String): LessonInfoWrap? {
        val list = getMiniLessonList()
        for (record in list) {
            if (record.lessonInfo.recordId == recordId) {
                return record
            }
        }
        return null
    }

}
