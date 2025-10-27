package cn.plaso.liveclasssdkdemo.resourcecenter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.upime.UpimeConfig
import cn.plaso.upime.UpimeObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URI

class CloudFileListFragment : Fragment() {
    private var isPreparsed: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private val adapter = FileListAdapter()
    private val okHttpClient by lazy { OkHttpClient.Builder().build() }

    // Static resource entries: prefer url; when absent, provide structured info
    private val RAW_RESOURCES: List<Map<String, Any>> by lazy {
        listOf(
            mapOf(
                "title" to "PPT解析第一版.pptx",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pptx"
            ),
            mapOf(
                "title" to "PPT解析第二版.pptx",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pptx"
            ),
            mapOf(
                "title" to "测试.pptx",
                "url" to "https://file.plaso.cn/upime/demo/file_center/9%E6%9C%882%E6%97%A5%E7%BE%8E%E6%96%87%E8%AF%B5%E8%AF%BB%E3%80%8A%E5%BF%83%E7%94%B0%E4%B8%8A%E7%9A%84%E7%99%BE%E5%90%88%E8%8A%B1%E3%80%8B%E5%B8%A6MP3.pptx"
            ),
            mapOf(
                "title" to "PDF.pdf",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pdf"
            ),
            mapOf(
                "title" to "WORD.docx",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.docx"
            ),
            mapOf(
                "title" to "EXCEL.xlsx",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.xlsx"
            ),
            mapOf(
                "title" to "图片.jpg",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.jpg"
            ),
            mapOf(
                "title" to "GIF.gif",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.gif"
            ),
            mapOf(
                "title" to "音频.mp3",
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.mp3"
            ),
            mapOf(
                "title" to "红杯烧水.mp4",
                "url" to "https://file.plaso.cn/upime/demo/file_center/strange.mp4"
            ),
            mapOf(
                "title" to "视频.mp4",
                "info" to listOf(
                    "struct",
                    mapOf(
                        "host" to "https://wwwr.plaso.cn/",
                        "path" to "static/cdn/sdk-drive-test/",
                        "fileName" to "test.mp4"
                    )
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPreparsed = arguments?.getBoolean(ARG_PREPARSED) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cloud_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.fileListView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        adapter.onItemClick = { item ->
            handleItemClick(item)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (isPreparsed) {
            fetchParsedData()
        } else {
            fetchData()
        }
    }

    private fun fetchParsedData() {
        val httpUrl = HttpUrl.Builder()
            .scheme("http")
            .host(PREPARSE_HOST)
            .port(PREPARSE_PORT)
            .addPathSegments(PREPARSE_PATH_BASE)
            .addPathSegment("list")
            .addQueryParameter("status", DEFAULT_STATUS)
            .addQueryParameter("limit", DEFAULT_LIMIT.toString())
            .addQueryParameter("offset", DEFAULT_OFFSET.toString())
            .build()
        val request = Request.Builder()
            .get()
            .addHeader("Accept", "application/json")
            .url(httpUrl)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "fetchParsedData onFailure", e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val bodyStr = res.body?.string() ?: ""
                    Log.i(TAG, "fetchParsedData onResponse: code=${res.code} , body=$bodyStr")
                    if (!res.isSuccessful) {
                        Log.e(TAG, "fetchParsedData onResponse not successful: code=${res.code}")
                        return
                    }
                    val list = parsePreparsedList(bodyStr)
                    if (!isAdded) return
                    requireActivity().runOnUiThread {
                        adapter.submit(list)
                    }
                }
            }
        })
    }

    private fun fetchData() {
        // Build list from static RAW_RESOURCES
        try {
            val list = ArrayList<FileItem>(RAW_RESOURCES.size)
            for (entry in RAW_RESOURCES) {
                val titleRaw = entry["title"] as? String ?: ""
                val url = entry["url"] as? String
                if (!url.isNullOrBlank()) {
                    val title = if (titleRaw.isBlank()) getFileNameFromUrl(url) else titleRaw
                    list.add(FileItem(title = title, url = url, totalPages = 0, id = "", preparsed = false))
                } else {
                    val infoVal = entry["info"]
                    val title = titleRaw
                    list.add(FileItem(title = title, url = "", totalPages = 0, id = "", preparsed = false, info = infoVal))
                }
            }
            if (!isAdded) return
            requireActivity().runOnUiThread { adapter.submit(list) }
        } catch (e: Exception) {
            Log.e(TAG, "fetchData build static list error", e)
            if (!isAdded) return
            requireActivity().runOnUiThread { adapter.submit(emptyList()) }
        }
    }

    private fun parsePreparsedList(json: String): List<FileItem> {
        return try {
            val root = JSONObject(json)
            val data = root.optJSONObject("data") ?: return emptyList()
            val files = data.optJSONArray("files") ?: return emptyList()
            val result = ArrayList<FileItem>(files.length())
            for (i in 0 until files.length()) {
                val item = files.optJSONObject(i) ?: continue
                val status = item.optString("status")
                val url = item.optString("url")
                if (status.equals("completed", ignoreCase = true) && !url.isNullOrEmpty()) {
                    val originalName = item.optString("originalName").takeIf { it.isNotEmpty() }
                    val fileName = item.optString("fileName").takeIf { it.isNotEmpty() }
                    val title = originalName ?: fileName ?: getFileNameFromUrl(url)
                    val totalPages = item.optInt("convertPages", 0)
                    val id = item.optString("id", "")
                    result.add(FileItem(title = title, url = url, totalPages = totalPages, id = id, preparsed = true) )
                }
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun handleItemClick(item: FileItem) {
        val title = item.title
        val url = item.url
        val extension = if (url.isNotBlank()) getFileExtFromUrl(url) else getFileExtFromTitle(title)
        val type = mapExtensionToType(extension)
        if (type == -1) {
            // unsupported type, ignore
            return
        }
        DemoApp.upimeBoard?.insertObject(UpimeObject().also {
            it.type = type
            if (!item.preparsed) {
                if (url.isNotBlank()) {
                    it.info = url
                } else if (item.info != null) {
                    it.info = item.info
                } else {
                    return@also
                }
            } else {
                val idMap = mutableMapOf<String, Any>()
                idMap["id"] = item.id
                val list = mutableListOf<Any>("parsed", idMap)
                it.info = list

                it.totalsPages = item.totalPages
                if (it.type == UpimeObject.TYPE_PPT) {
                    it.type = UpimeObject.TYPE_I_SPRING_PPT
                }
            }
            it.title = title
        })
        requireActivity().finish()
    }

    private fun getFileNameFromUrl(url: String): String {
        return try {
            val uri = URI(url)
            val path = uri.path
            val last = path.substringAfterLast('/')
            if (last.isNotEmpty()) last else url
        } catch (_: Exception) {
            url
        }
    }

    private fun getFileExtFromUrl(url: String): String {
        val base = url.substringBefore('#').substringBefore('?')
        val last = base.substringAfterLast('/')
        val ext = last.substringAfterLast('.', "")
        return ext.lowercase()
    }

    private fun getFileExtFromTitle(title: String): String {
        val dot = title.lastIndexOf('.')
        return if (dot >= 0 && dot < title.length - 1) title.substring(dot + 1).lowercase() else ""
    }

    private fun mapExtensionToType(extension: String): Int {
        val openFileMode = DemoApp.sp.getInt("openFileMode", UpimeConfig.OPEN_FILE_MODE_IMAGE)
        val isWindow = openFileMode == UpimeConfig.OPEN_FILE_MODE_WINDOW
        return when (extension) {
            "pdf" -> UpimeObject.TYPE_PDF
            "doc", "docx" -> if (isWindow) UpimeObject.TYPE_DOC else UpimeObject.TYPE_WORD
            "xls", "xlsx" -> if (isWindow) UpimeObject.TYPE_XLS else UpimeObject.TYPE_EXCEL
            "jpg", "jpeg", "png", "gif" -> UpimeObject.TYPE_IMAGE
            "mp3" -> UpimeObject.TYPE_AUDIO
            "mp4" -> UpimeObject.TYPE_VIDEO
            "ppt", "pptx" -> if (DemoApp.newPPt) UpimeObject.TYPE_DX_PPT  else UpimeObject.TYPE_PPT
            else -> -1
        }
    }

    companion object {
        const val TAG: String = "CloudFileListFragment"
        private const val ARG_PREPARSED = "arg_preparsed"
        private const val NON_PREPARSE_URL = "https://dev.plaso.cn/ossutilserver/getFileList"
        private const val PREPARSE_HOST = "120.55.3.51"
        private const val PREPARSE_PORT = 3000
        private const val PREPARSE_PATH_BASE = "api/files"
        private const val DEFAULT_STATUS = "completed"
        private const val DEFAULT_LIMIT = 10000
        private const val DEFAULT_OFFSET = 0

        fun newInstance(isPreparsed: Boolean): CloudFileListFragment {
            return CloudFileListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PREPARSED, isPreparsed)
                }
            }
        }
    }
}
