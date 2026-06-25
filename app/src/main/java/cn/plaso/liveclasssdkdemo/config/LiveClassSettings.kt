package cn.plaso.liveclasssdkdemo.config

import android.content.Context
import android.content.SharedPreferences
import cn.plaso.upime.UpimeConfig
import androidx.core.content.edit
import cn.plaso.data.LiveClassConfig.Companion.VD_360P

object LiveClassSettings {
    private const val PREF_NAME = "plaso_live_class_config"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var meetingType: String
        get() = sp.getString("meeting_type", "video") ?: "video"
        set(value) = sp.edit { putString("meeting_type", value) }

    var userType: String
        get() = sp.getString("user_role", "listener") ?: "listener"
        set(value) = sp.edit { putString("user_role", value) }

    var openFileMode: Int
        get() = sp.getInt("open_file_mode", UpimeConfig.OPEN_FILE_MODE_WINDOW)
        set(value) = sp.edit { putInt("open_file_mode", value) }

    var enablePptInteract: Boolean
        get() = sp.getBoolean("enable_ppt_interact", false)
        set(value) = sp.edit { putBoolean("enable_ppt_interact", value) }

    var useDxPptParse: Boolean
        get() = sp.getBoolean("use_dx_ppt_parse", false)
        set(value) = sp.edit { putBoolean("use_dx_ppt_parse", value) }

    var undoSupport: Boolean
        get() = sp.getBoolean("undo_support", false)
        set(value) = sp.edit { putBoolean("undo_support", value) }

    var useNewSmallBoard: Boolean
        get() = sp.getBoolean("use_new_small_board", false)
        set(value) = sp.edit { putBoolean("use_new_small_board", value) }

    var supportSelect: Boolean
        get() = sp.getBoolean("support_select", true)
        set(value) = sp.edit { putBoolean("support_select", value) }

    var toolBoxItems: Int
        get() = sp.getInt("tool_box_items", 0)
        set(value) = sp.edit(commit = true) { putInt("tool_box_items", value) }

    fun hasToolBoxItems(): Boolean {
        return sp.contains("tool_box_items")
    }

    var teachToolTypes: Int
        get() = sp.getInt("teach_tool_types", 0)
        set(value) = sp.edit { putInt("teach_tool_types", value) }

    var videoDefinition: String
        get() = sp.getString("video_definition", VD_360P) ?: VD_360P
        set(value) = sp.edit { putString("video_definition", value) }

    var editElementMode: Int
        get() = sp.getInt("edit_element_mode", 0)
        set(value) = sp.edit { putInt("edit_element_mode", value) }

    var supportHighlighter: Boolean
        get() = sp.getBoolean("support_highlighter", false)
        set(value) = sp.edit { putBoolean("support_highlighter", value) }

    var localFileSupport: Boolean
        get() = sp.getBoolean("local_file_support", false)
        set(value) = sp.edit { putBoolean("local_file_support", value) }

    var isResidentCamera: Boolean
        get() = sp.getBoolean("is_resident_camera", true)
        set(value) = sp.edit { putBoolean("is_resident_camera", value) }

    var enableSaveBoard: Boolean
        get() = sp.getBoolean("enable_save_board", false)
        set(value) = sp.edit { putBoolean("enable_save_board", value) }

    var enableVote: Boolean
        get() = sp.getBoolean("enable_vote", false)
        set(value) = sp.edit { putBoolean("enable_vote", value) }

    var enableLiveSign: Boolean
        get() = sp.getBoolean("enable_live_sign", false)
        set(value) = sp.edit { putBoolean("enable_live_sign", value) }
}
