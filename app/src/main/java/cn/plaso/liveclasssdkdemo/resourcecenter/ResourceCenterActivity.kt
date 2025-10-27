package cn.plaso.liveclasssdkdemo.resourcecenter

import android.app.Activity
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


const val LOCAL_RESOURCE_REQUEST_CODE = 0x01
const val TAG = "ResourceCenterActivity"

class ResourceCenterActivity : AppCompatActivity() {


    val NETWORK_FILE_TYPE = mutableMapOf<String,String>()
    init {
        NETWORK_FILE_TYPE.put("application/pdf" , "pdf")
        NETWORK_FILE_TYPE.put("application/x-xls" , "xls")
        NETWORK_FILE_TYPE.put("application/msword" , "doc")
        NETWORK_FILE_TYPE.put("application/x-png" , "png")
        NETWORK_FILE_TYPE.put("application/x-ppt" , "ppt")
        NETWORK_FILE_TYPE.put("application/vnd.ms-powerpoint" , "ppt")
        NETWORK_FILE_TYPE.put("application/vnd.ms-powerpoint" , "ppt")
        NETWORK_FILE_TYPE.put("audio/mp3" , "mp3")
        NETWORK_FILE_TYPE.put("video/mpeg4" , "mp4")
        NETWORK_FILE_TYPE.put("video/mp4" , "mp4")
        NETWORK_FILE_TYPE.put("image/gif" , "gif")
        NETWORK_FILE_TYPE.put("image/png" , "png")
        NETWORK_FILE_TYPE.put("image/jpeg" , "jpg")
    }

    lateinit var mOkhttpClient :OkHttpClient
    lateinit var etInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_center)

        etInput = findViewById(R.id.etInput)

        var btnInsertImage = findViewById<Button>(R.id.btnInsertImage)
        btnInsertImage.setOnClickListener {
            insertNetWorkFile(false)
        }
        var btnLocalResource = findViewById<Button>(R.id.btnLocalResource)
        btnLocalResource.setOnClickListener {
            FilePickerManager
                .from(this@ResourceCenterActivity)
                .enableSingleChoice()
                .forResult(FilePickerManager.REQUEST_CODE)
        }
        var btnLocalSecResource = findViewById<Button>(R.id.btnLocalSecResource)
        btnLocalSecResource.setOnClickListener{
            insertNetWorkFile(true)
        }

        mOkhttpClient = OkHttpClient.Builder().build()

    }



    private fun insertNetWorkFile(secure: Boolean) {
        val urlLocation = etInput.text.toString()

//        //todo 测试代码
//        etInput.setText("https://file-plaso.oss-cn-hangzhou.aliyuncs.com/test-plaso%2FprogramTempDir%2FupimeTempDir%2Ftest_s.plaso.cn%2F38870_1610952409120_test_s%2F1610957077172_265.docx?Expires=1610957177&OSSAccessKeyId=STS.NTAFtwf1Tu31qoERmJfLKeMq4&Signature=GwLoovkfSWZLV2%2FpNHnPZDmcFlQ%3D&security-token=CAISrQJ1q6Ft5B2yfSjIr5f0Dc7Di%2B51wvHac0n0tm0fasNnioja1jz2IHFIfXNtAOAftPo%2BmGpT5vwZlqZvRoRZcleCdc959ZMR%2BgX5nW0ldlXzv9I%2Bk5SANTW5G3yShb3JAYjQSNfaZY3iCTTtnTNyxr3XbCirW0ffX7SClZ9gaKZwPGy%2FdiEUK9pKAQFgpcQGT1KzU8ygKRn3mGHdIVN1sw5n8wNF5L%2B439eX52iT7hbzwfRHoJ%2FqcNr2LZtiPZNyFs%2Bx1fdxMbbMyzIV8xxD76Axzo48oGeb443AXggPv03Xb7CJrOcCdlEpOvIIfIdft%2BX5mPFCvejeqp%2F60R4lP5sOAniGHt35kJKeRrL1bI1pL%2B3hQnPWyZWVO5D5tAUgcDcBMwdHawK3go7zrNdHGoABbHVtjhMW5lnAZPzEAAgjzB8hYMCC0NBuSUSdJg7hnzPm3vq8dm0GSGmbYqt0YQRrU9fhgp7Mz5t5GlxFtxQerhK0gc4QzpMIcK7d73Q9YL0H9f78k1ca7PKNYAXc1LZk86%2FrkkZrFOz%2BQY%2FhaeOoB0ahFS6Zq2pdPecq0ss8e98%3D")

        if(TextUtils.isEmpty(urlLocation)){
            Toast.makeText(this@ResourceCenterActivity, "文件为空", Toast.LENGTH_SHORT)
                .show()
        }
        lifecycleScope.launch {
            val netWorkFileType = getNetWorkFileType(urlLocation)
            Log.i(TAG, "insertNetWorkFile: ${netWorkFileType}")
            val extension = NETWORK_FILE_TYPE[netWorkFileType]
            if(TextUtils.isEmpty(extension)){
                Toast.makeText(this@ResourceCenterActivity, "不支持该类型", Toast.LENGTH_SHORT)
                    .show()
            }else {
                var file = ""
                val fileType = getFileType(extension!!)
                if(secure){
                    var json = JSONObject()
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

        var request = Request.Builder().url(urlLocation).get().build()
        val call = mOkhttpClient.newCall(request)
        val response = call.execute()
        val contenType = response.header("Content-type")
        contenType!!
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    // do your work
                    println(list)
                    if (list.size > 0) {
                        var path = list.get(0)
                        insertLocalFile(path)
                        finish()
                    }else{
                        Toast.makeText(this@ResourceCenterActivity, "没有选择任何东西~", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this@ResourceCenterActivity, "没有选择任何东西~", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun insertLocalFile(filePath: String?) {
        if(TextUtils.isEmpty(filePath)){
            Toast.makeText(this@ResourceCenterActivity, "没有选择任何东西~", Toast.LENGTH_SHORT)
                .show()
            return
        }

        var file = File(filePath)
        var extension = getFileExt(filePath!!)
        val fileType = getFileType(extension!!)
        Log.d(TAG, "insertLocalFile fileType : " + fileType);
        if(fileType == -1){
            Toast.makeText(this@ResourceCenterActivity, "不支持该类型", Toast.LENGTH_SHORT)
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
        val p: Int = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'))
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