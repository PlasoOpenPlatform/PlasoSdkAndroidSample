package cn.plaso.liveclasssdkdemo.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cn.plaso.upime.UpimeParameter

/**
 * 实时课堂高级设置配置
 * 所有配置项通过 SharedPreferences 持久化保存
 */
object AdvancedConfig {
    private const val PREF_NAME = "plaso_advanced_config"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var rtcType: Int
        get() = sp.getInt("rtc_type", 2)
        set(value) = sp.edit { putInt("rtc_type", value) }

    var hostAddr: String
        get() = sp.getString("host_addr", "") ?: ""
        set(value) = sp.edit { putString("host_addr", value) }

    var logLevel: Int
        get() = sp.getInt("log_level", UpimeParameter.INFO)
        set(value) = sp.edit { putInt("log_level", value) }

    var enableUploadLog: Boolean
        get() = sp.getBoolean("enable_upload_log", true)
        set(value) = sp.edit { putBoolean("enable_upload_log", value) }

    var liveStreaming: Boolean
        get() = sp.getBoolean("live_streaming", false)
        set(value) = sp.edit { putBoolean("live_streaming", value) }

    var mobileTeaching: Boolean
        get() = sp.getBoolean("mobile_teaching", false)
        set(value) = sp.edit { putBoolean("mobile_teaching", value) }

    var auxiliaryCamera: Boolean
        get() = sp.getBoolean("auxiliary_camera", true)
        set(value) = sp.edit { putBoolean("auxiliary_camera", value) }

    var enableRearCamera: Boolean
        get() = sp.getBoolean("enable_rear_camera", false)
        set(value) = sp.edit { putBoolean("enable_rear_camera", value) }

    var lowLatencyClass: Boolean
        get() = sp.getBoolean("low_latency_class", false)
        set(value) = sp.edit { putBoolean("low_latency_class", value) }

    var newQuizMode: Int
        get() = sp.getInt("new_quiz_mode", 0).coerceIn(0, 3)
        set(value) {
            val mode = value.coerceIn(0, 3)
            sp.edit { putInt("new_quiz_mode", mode) }
        }

    var enableStarShow: Boolean
        get() = sp.getBoolean("enable_star_show", false)
        set(value) = sp.edit { putBoolean("enable_star_show", value) }

    var enableRecallMsg: Boolean
        get() = sp.getBoolean("enable_recall_msg", false)
        set(value) = sp.edit { putBoolean("enable_recall_msg", value) }

    var enableLiveclassSignRollcall: Boolean
        get() = sp.getBoolean("enable_liveclass_sign_rollcall", false)
        set(value) = sp.edit { putBoolean("enable_liveclass_sign_rollcall", value) }

    var hideRecordingIndicator: Boolean
        get() = sp.getBoolean("hide_recording_indicator", false)
        set(value) = sp.edit { putBoolean("hide_recording_indicator", value) }

    var showStudentClassElapsedTime: Boolean
        get() = sp.getBoolean("show_student_class_elapsed_time", false)
        set(value) = sp.edit { putBoolean("show_student_class_elapsed_time", value) }

    var useMeetingMode: Boolean
        get() = sp.getBoolean("use_meeting_mode", false)
        set(value) = sp.edit { putBoolean("use_meeting_mode", value) }

    var meetingDefaultPermission: Int
        get() = sp.getInt("meeting_default_permission", 0)
        set(value) = sp.edit { putInt("meeting_default_permission", value) }

    var enableLiveOfflineSign: Boolean
        get() = sp.getBoolean("enable_live_offline_sign", false)
        set(value) = sp.edit { putBoolean("enable_live_offline_sign", value) }

    var endRemindTime: Int
        get() = sp.getInt("end_remind_time", 0)
        set(value) = sp.edit { putInt("end_remind_time", value) }

    var pptType: Int
        get() = sp.getInt("ppt_type", 1)
        set(value) = sp.edit { putInt("ppt_type", value) }

    var resolution: String
        get() = sp.getString("common_resolution", "1280x720") ?: "1280x720"
        set(value) = sp.edit { putString("common_resolution", value) }

    var onlineMode: Int
        get() = sp.getInt("online_mode", 1)
        set(value) = sp.edit { putInt("online_mode", value) }

    var keyForColor: String
        get() = sp.getString("key_for_color", "") ?: ""
        set(value) = sp.edit { putString("key_for_color", value) }

    var redPacketLimit: Int
        get() = sp.getInt("red_packet_limit", 0)
        set(value) = sp.edit { putInt("red_packet_limit", value) }
    
    var canShareBoardToHistory: Boolean
        get() = sp.getBoolean("can_share_board_to_history", false)
        set(value) = sp.edit { putBoolean("can_share_board_to_history", value) }
}
