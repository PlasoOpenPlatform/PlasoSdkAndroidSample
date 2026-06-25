package cn.plaso.liveclasssdkdemo.resourcecenter

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.upime.UpimeObject
import cn.plaso.upime.UpimeObject.TYPE_AUDIO
import cn.plaso.upime.UpimeObject.TYPE_EXCEL
import cn.plaso.upime.UpimeObject.TYPE_IMAGE
import cn.plaso.upime.UpimeObject.TYPE_PDF
import cn.plaso.upime.UpimeObject.TYPE_PPT
import cn.plaso.upime.UpimeObject.TYPE_VIDEO
import cn.plaso.upime.UpimeObject.TYPE_WORD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rosuh.filepicker.config.FilePickerManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
const val TAG = "ResourceCenterActivity"

class ResourceCenterActivity : AppCompatActivity() {


    val networkFileType = mutableMapOf<String,String>()
    init {
        networkFileType["application/pdf"] = "pdf"
        networkFileType["application/x-xls"] = "xls"
        networkFileType["application/msword"] = "doc"
        networkFileType["application/x-png"] = "png"
        networkFileType["application/x-ppt"] = "ppt"
        networkFileType["application/vnd.ms-powerpoint"] = "ppt"
        networkFileType["application/vnd.ms-powerpoint"] = "ppt"
        networkFileType["audio/mp3"] = "mp3"
        networkFileType["video/mpeg4"] = "mp4"
        networkFileType["video/mp4"] = "mp4"
        networkFileType["image/gif"] = "gif"
        networkFileType["image/png"] = "png"
        networkFileType["image/jpeg"] = "jpg"
    }

    lateinit var mOkhttpClient :OkHttpClient
    lateinit var etInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_center)

        etInput = findViewById(R.id.etInput)

        val btnInsertImage = findViewById<Button>(R.id.btnInsertImage)
        btnInsertImage.setOnClickListener {
            insertNetWorkFile(false)
        }
        val btnLocalResource = findViewById<Button>(R.id.btnLocalResource)
        btnLocalResource.setOnClickListener {
            FilePickerManager
                .from(this@ResourceCenterActivity)
                .enableSingleChoice()
                .forResult(FilePickerManager.REQUEST_CODE)
        }
        val btnLocalSecResource = findViewById<Button>(R.id.btnLocalSecResource)
        btnLocalSecResource.setOnClickListener{
            insertNetWorkFile(true)
        }

        mOkhttpClient = OkHttpClient.Builder().build()

    }



    private fun insertNetWorkFile(secure: Boolean) {
        val urlLocation = etInput.text.toString()

        if(TextUtils.isEmpty(urlLocation)){
            Toast.makeText(this@ResourceCenterActivity, getString(R.string.file_empty), Toast.LENGTH_SHORT)
                .show()
        }
        lifecycleScope.launch {
            val netWorkFileType = getNetWorkFileType(urlLocation)
            Log.i(TAG, "insertNetWorkFile: $netWorkFileType")
            val extension = networkFileType[netWorkFileType]
            if(TextUtils.isEmpty(extension)){
                Toast.makeText(this@ResourceCenterActivity, getString(R.string.unsupported_type), Toast.LENGTH_SHORT)
                    .show()
            }else {
                var file: String
                val fileType = getFileType(extension!!)
                if(secure){
                    val json = JSONObject()
                    json.put("file",urlLocation)
                    file = json.toString()
                }else{
                    file = urlLocation
                }

                insertFile(fileType,file,extension)
                finish()
            }
        }

    }

    private suspend fun getNetWorkFileType(urlLocation:String) = withContext(Dispatchers.IO){

        val request = Request.Builder().url(urlLocation).get().build()
        val call = mOkhttpClient.newCall(request)
        val response = call.execute()
        val contenType = response.header("Content-type")
        contenType!!
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    // do your work
                    println(list)
                    if (list.isNotEmpty()) {
                        val path = list[0]
                        insertLocalFile(path)
                        finish()
                    }else{
                        Toast.makeText(this@ResourceCenterActivity, getString(R.string.nothing_selected), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this@ResourceCenterActivity, getString(R.string.nothing_selected), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun insertLocalFile(filePath: String?) {
        if(TextUtils.isEmpty(filePath)){
            Toast.makeText(this@ResourceCenterActivity, getString(R.string.nothing_selected), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val file = File(filePath!!)
        val extension = getFileExt(filePath)
        val fileType = getFileType(extension)
        Log.d(TAG, "insertLocalFile fileType : $fileType");
        if(fileType == -1){
            Toast.makeText(this@ResourceCenterActivity, getString(R.string.unsupported_type), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val name = file.name
        insertFile(fileType, filePath, name)
    }

    private fun insertFile(fileType: Int, filePath: String?, name: String) {
        DemoApp.upimeBoard?.insertObject(UpimeObject().also {
            it.type = fileType
            it.info = filePath
            it.title = name
        })
    }

    /**
     * 获取文件后缀
     */
    fun getFileExt(filePath:String):String{
        var extension = ""
        val i: Int = filePath.lastIndexOf('.')
        val p: Int = filePath.lastIndexOf('/').coerceAtLeast(filePath.lastIndexOf('\\'))
        if (i > p) {
            extension = filePath.substring(i + 1)
        }

        return extension
    }

    fun getFileType(extension: String):Int{

        var fileType = -1
        when(extension){
            "pdf" -> {
                fileType = TYPE_PDF
            }
            "doc" -> {
                fileType = TYPE_WORD
            }
            "docx" -> {
                fileType = TYPE_WORD
            }
            "jpg"->{
                fileType = TYPE_IMAGE
            }
            "png"->{
                fileType = TYPE_IMAGE
            }
            "gif"->{
                fileType = TYPE_IMAGE
            }
            "xls" ->{
                fileType = TYPE_EXCEL
            }
            "xlsx" ->{
                fileType = TYPE_EXCEL
            }
            "mp3"->{
                fileType = TYPE_AUDIO
            }
            "mp4" ->{
                fileType = TYPE_VIDEO
            }
            "ppt","pptx" ->{
                fileType = TYPE_PPT
            }
        }
        return fileType
    }


}