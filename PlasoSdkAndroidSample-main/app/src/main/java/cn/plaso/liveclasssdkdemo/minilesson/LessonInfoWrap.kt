package cn.plaso.liveclasssdkdemo.minilesson

import android.text.format.DateUtils
import cn.plaso.liveclasssdkdemo.DemoApp.Companion.GSON
import cn.plaso.upime.LessonInfo

const val EDITING = 1
const val RECORDED = 2
const val UPLOADED = 3
const val UPLOADING = 4

class LessonInfoWrap(var lessonInfo: LessonInfo,
                     var localPath:String,
                     var status: Int = EDITING,
                     var progress:Int? = null) {
    override fun toString(): String {
        return GSON.toJson(this)
    }

    fun getDuration(): String {
        return DateUtils.formatElapsedTime(lessonInfo.duration.toLong() / 1000)
    }

    fun getName(): CharSequence? {
        return lessonInfo.topic
    }
}

fun LessonInfo.wrap(localPath:String,status: Int = EDITING): LessonInfoWrap {
    return LessonInfoWrap(lessonInfo = this,localPath = localPath, status = status)
}