package cn.plaso.liveclasssdkdemo.resourcecenter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.plaso.Globals
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI

class CloudFileListFragment : Fragment() {
    private var isPreparsed: Boolean = false
    private val isWindowFileMode: Boolean
        get() = Globals.getOpenFileMode() == UpimeConfig.OPEN_FILE_MODE_WINDOW
    private lateinit var recyclerView: RecyclerView
    private val adapter = FileListAdapter()
    private val okHttpClient by lazy { OkHttpClient.Builder().build() }

    // Static resource entries: prefer url; when absent, provide structured info
    private val rawResources: List<Map<String, Any>> by lazy {
        listOf(
            mapOf(
                "titleRes" to R.string.cloud_file_ppt_v1,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pptx"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_ppt_v2,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pptx"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_pptx,
                "url" to "https://file.plaso.cn/upime/demo/file_center/9%E6%9C%882%E6%97%A5%E7%BE%8E%E6%96%87%E8%AF%B5%E8%AF%BB%E3%80%8A%E5%BF%83%E7%94%B0%E4%B8%8A%E7%9A%84%E7%99%BE%E5%90%88%E8%8A%B1%E3%80%8B%E5%B8%A6MP3.pptx"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_pdf,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.pdf"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_word,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.docx"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_excel,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.xlsx"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_image_jpg,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.jpg"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_gif,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.gif"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_audio_mp3,
                "url" to "https://wwwr.plaso.cn/static/cdn/sdk-drive-test/test.mp3"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_red_cup_video,
                "url" to "https://file.plaso.cn/upime/demo/file_center/strange.mp4"
            ),
            mapOf(
                "title" to "机器学习的一般流程与NumPy安装.mp4",
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/51_%E6%9C%BA%E5%99%A8%E5%AD%A6%E4%B9%A0%E7%9A%84%E4%B8%80%E8%88%AC%E6%B5%81%E7%A8%8B%E4%B8%8ENumPy%E5%AE%89%E8%A3%85.mp4"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_video_mp4,
                "info" to listOf(
                    "struct",
                    mapOf(
                        "host" to "https://wwwr.plaso.cn/",
                        "path" to "static/cdn/sdk-drive-test/",
                        "fileName" to "test.mp4"
                    )
                )
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_avi,
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/%E8%A7%86%E9%A2%912.avi"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_mov,
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/%E8%A7%86%E9%A2%911.mov"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_webm,
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/%E8%A7%86%E9%A2%911.webm"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_mpg,
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/%E8%A7%86%E9%A2%914.mpg"
            ),
            mapOf(
                "titleRes" to R.string.cloud_file_test_m2a,
                "url" to "http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/test.m4a"
            ),

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
            val list = ArrayList<FileItem>(rawResources.size)
            for (entry in rawResources) {
                val titleRes = entry["titleRes"] as? Int
                val titleRaw = titleRes?.let { getString(it) } ?: (entry["title"] as? String ?: "")
                val url = entry["url"] as? String
                if (!url.isNullOrBlank()) {
                    val title = titleRaw.ifBlank { getFileNameFromUrl(url) }
                    list.add(
                        FileItem(
                            title = title,
                            url = url,
                            totalPages = 0,
                            id = "",
                            preparsed = false
                        )
                    )
                } else {
                    val infoVal = entry["info"]
                    val title = titleRaw
                    list.add(
                        FileItem(
                            title = title,
                            url = "",
                            totalPages = 0,
                            id = "",
                            preparsed = false,
                            info = infoVal
                        )
                    )
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
                    result.add(
                        FileItem(
                            title = title,
                            url = url,
                            totalPages = totalPages,
                            id = id,
                            preparsed = true
                        )
                    )
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
        val mappedType = mapExtensionToType(extension)
        if (mappedType == -1) {
            // unsupported type, ignore
            return
        }
        val insertType = if (item.preparsed &&
            (mappedType == UpimeObject.TYPE_PPT || mappedType == UpimeObject.TYPE_DX_PPT)
        ) {
            UpimeObject.TYPE_I_SPRING_PPT
        } else {
            mappedType
        }

        // 图片模式下需先下载到本地再插入：非预解析 word/excel；预解析 pdf/doc/excel 先拉签名 URL
        if (shouldDownloadBeforeInsert(item, insertType, url)) {
            val shouldConvertToPdf = insertType == UpimeObject.TYPE_WORD ||
                insertType == UpimeObject.TYPE_EXCEL
            val totalPages = if (item.preparsed) 0 else null
            if (item.preparsed) {
                PreParseFileHelper.fetchSignedOriginalDocumentUrl(item.id) { signedUrl ->
                    if (!isAdded) return@fetchSignedOriginalDocumentUrl
                    requireActivity().runOnUiThread {
                        if (signedUrl.isNullOrBlank()) {
                            Log.e(TAG, "fetchSignedOriginalDocumentUrl failed, fileId=${item.id}")
                            return@runOnUiThread
                        }
                        downloadAndInsertObject(
                            signedUrl,
                            title,
                            insertType,
                            shouldConvertToPdf,
                            totalPages
                        )
                    }
                }
            } else {
                downloadAndInsertObject(url, title, insertType, shouldConvertToPdf, totalPages)
            }
            return
        }

        DemoApp.upimeBoard?.insertObject(UpimeObject().also {
            it.type = insertType
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
                val list = mutableListOf("parsed", idMap)
                it.info = list

                it.totalsPages = item.totalPages
            }
            it.title = title
        })
        requireActivity().finish()
    }

    private fun shouldDownloadBeforeInsert(item: FileItem, insertType: Int, url: String): Boolean {
        if (isWindowFileMode) {
            return false
        }
        return if (item.preparsed) {
            item.id.isNotBlank() &&
                (insertType == UpimeObject.TYPE_PDF ||
                    insertType == UpimeObject.TYPE_WORD ||
                    insertType == UpimeObject.TYPE_EXCEL)
        } else {
            url.isNotBlank() &&
                url.startsWith("http", ignoreCase = true) &&
                (insertType == UpimeObject.TYPE_WORD || insertType == UpimeObject.TYPE_EXCEL)
        }
    }

    private fun downloadAndInsertObject(
        url: String,
        title: String,
        type: Int,
        shouldConvertToPdf: Boolean = true,
        totalPages: Int? = null
    ) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download file failed: $url", e)
                if (!isAdded) return
                requireActivity().runOnUiThread {
                    // Fallback to using the URL directly
                    insertObjectWithInfo(url, title, type, shouldConvertToPdf, totalPages)
                    requireActivity().finish()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e(TAG, "Download file failed with code: ${res.code}")
                        if (!isAdded) return
                        requireActivity().runOnUiThread {
                            // Fallback to using the URL directly
                            insertObjectWithInfo(url, title, type, shouldConvertToPdf, totalPages)
                            requireActivity().finish()
                        }
                        return
                    }

                    try {
                        val fileName = getFileNameFromUrl(url)
                        val localFile = File(requireContext().cacheDir, fileName)
                        FileOutputStream(localFile).use { output ->
                            res.body?.byteStream()?.copyTo(output)
                        }

                        val localPath = localFile.absolutePath
                        Log.i(TAG, "File downloaded to: $localPath")

                        if (!isAdded) return
                        requireActivity().runOnUiThread {
                            insertObjectWithInfo(localPath, title, type, shouldConvertToPdf, totalPages)
                            requireActivity().finish()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving downloaded file", e)
                        if (!isAdded) return
                        requireActivity().runOnUiThread {
                            // Fallback to using the URL directly
                            insertObjectWithInfo(url, title, type, shouldConvertToPdf, totalPages)
                            requireActivity().finish()
                        }
                    }
                }
            }
        })
    }

    private fun insertObjectWithInfo(
        info: String,
        title: String,
        type: Int,
        shouldConvertToPdf: Boolean = false,
        totalPages: Int? = null
    ) {
        DemoApp.upimeBoard?.insertObject(UpimeObject().also {
            it.type = type
            it.info = info
            it.title = title
            if (shouldConvertToPdf) {
                it.shouldConvertToPdf = true
            }
            if (totalPages != null) {
                it.totalsPages = totalPages
            }
        })
    }

    private fun getFileNameFromUrl(url: String): String {
        return try {
            val uri = URI(url)
            val path = uri.path
            val last = path.substringAfterLast('/')
            last.ifEmpty { url }
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
        return when (extension) {
            "pdf" -> UpimeObject.TYPE_PDF
            "doc", "docx" -> if (isWindowFileMode) UpimeObject.TYPE_DOC else UpimeObject.TYPE_WORD
            "xls", "xlsx" -> if (isWindowFileMode) UpimeObject.TYPE_XLS else UpimeObject.TYPE_EXCEL
            "jpg", "jpeg", "png", "gif" -> UpimeObject.TYPE_IMAGE
            "mp3" -> UpimeObject.TYPE_AUDIO
            "mp4" -> UpimeObject.TYPE_VIDEO
            "ppt", "pptx" -> if (DemoApp.newPPt) UpimeObject.TYPE_DX_PPT else UpimeObject.TYPE_PPT
            else -> -1
        }
    }

    companion object {
        const val TAG: String = "CloudFileListFragment"
        private const val ARG_PREPARSED = "arg_preparsed"
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
