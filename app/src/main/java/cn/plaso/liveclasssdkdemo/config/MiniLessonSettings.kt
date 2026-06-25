package cn.plaso.liveclasssdkdemo.config

import android.content.Context
import android.content.SharedPreferences
import cn.plaso.upime.UpimeConfig
import androidx.core.content.edit

object MiniLessonSettings {
    const val RECORD_TYPE_AUDIO = 1
    const val RECORD_TYPE_VIDEO = 2
    
    private const val PREF_NAME = "plaso_mini_lesson_config"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

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

    var recordType: Int
        get() = sp.getInt("record_type", 1) // AUDIO
        set(value) = sp.edit { putInt("record_type", value) }

    var supportSaveDraft: Boolean
        get() = sp.getBoolean("support_save_draft", true)
        set(value) = sp.edit { putBoolean("support_save_draft", value) }

    var teachToolTypes: Int
        get() = sp.getInt("teach_tool_types", 0)
        set(value) = sp.edit { putInt("teach_tool_types", value) }

    var editElementMode: Int
        get() = sp.getInt("edit_element_mode", 0)
        set(value) = sp.edit { putInt("edit_element_mode", value) }

    var supportHighlighter: Boolean
        get() = sp.getBoolean("support_highlighter", false)
        set(value) = sp.edit { putBoolean("support_highlighter", value) }

    var localFileSupport: Boolean
        get() = sp.getBoolean("local_file_support", false)
        set(value) = sp.edit { putBoolean("local_file_support", value) }

    var keyForColor: String
        get() = sp.getString("key_for_color", "") ?: ""
        set(value) = sp.edit { putString("key_for_color", value) }

    var ossToken: String
        get() = sp.getString("oss_token", "") ?: ""
        set(value) = sp.edit { putString("oss_token", value) }
}
