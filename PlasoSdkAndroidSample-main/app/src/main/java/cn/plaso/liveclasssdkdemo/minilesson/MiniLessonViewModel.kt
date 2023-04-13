package cn.plaso.liveclasssdkdemo.minilesson

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.upime.LessonInfo
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch

class MiniLessonViewModel : ViewModel() {

    var configKey: String = "default";
    var lessonList = MutableLiveData<MutableList<LessonInfoWrap>>()
    fun getMiniLessonList() {
        viewModelScope.launch {
            DemoApp.upime.getDraftList(configKey) {
                val draftList = it.map {
                    LessonInfoWrap(it, it.path, transformStatus(it.status), null);
                }.toMutableList()

                val uploaded = DemoApp.app.getMiniLessonList();

                val all = mutableListOf<LessonInfoWrap>()
                all.addAll(uploaded)
                all.addAll(draftList)
                lessonList.postValue(all)
            }

        }
    }

    private fun transformStatus(status: Int): Int {
        if (status == LessonInfo.STATUS_FINISHED) {
            return RECORDED
        }
        return EDITING
    }

    fun deleteLesson(it: LessonInfoWrap) {
        viewModelScope.launch {
            if (it.status == RECORDED || it.status == EDITING) {
                DemoApp.upime.deleteDraft(configKey, it.lessonInfo.path)
            } else {
                DemoApp.app.deleteLessonInfo(it)
            }
            getMiniLessonList()
        }
    }

    fun uploadFinish(recordId: String?, lesson: LessonInfo?, localPath: String) {
        if (recordId == null || lesson == null) {
            return
        }
        viewModelScope.launch {
            val it = LessonInfoWrap(lesson, lesson.path, UPLOADED, 100)
            DemoApp.app.saveLessonInfo(it)
            DemoApp.upime.deleteDraft(configKey, localPath, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    getMiniLessonList()
                }

                override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                }

                override fun notImplemented() {
                }

            })

        }
    }

}