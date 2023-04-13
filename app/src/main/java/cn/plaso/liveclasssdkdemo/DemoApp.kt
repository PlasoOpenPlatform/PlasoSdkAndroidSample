package cn.plaso.liveclasssdkdemo

import android.app.Application
import android.content.SharedPreferences
import cn.plaso.liveclasssdkdemo.minilesson.LessonInfoWrap
import cn.plaso.upime.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DemoApp : Application() {
    companion object {
        var upimeBoard: UpimeBoard? = null
        const val TAG = "DemoApp"
        lateinit var sp: SharedPreferences
        val GSON = Gson()
        lateinit var app: DemoApp
        lateinit var upime: StyleUpime
        var newPPt: Boolean = false
    }

    lateinit var resProvider: IResourceProvider
    override fun onCreate() {
        super.onCreate()
        app = this
        sp = getSharedPreferences("demo", MODE_PRIVATE)

        resProvider = object : IResourceProvider {
            override fun supportResourceCenter(): Boolean {
                return true
            }

            override fun showResourceCenter() {
            }

            override fun dismissResourceCenter() {
            }

            override fun getExtFileName(extFileId: Any?, resouceCallback:IResourceCallback?) {
                println("getExtFileName $extFileId")
                /* if (extFileId is List<Any?>) {
                     val x = extFileId[1] as Map<String, Any>
                     resouceCallback?.onFilePath("${x["host"]}${x["path"]}${x["fileName"]}")
                 }*/
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