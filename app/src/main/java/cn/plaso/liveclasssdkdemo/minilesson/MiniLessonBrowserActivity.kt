package cn.plaso.liveclasssdkdemo.minilesson

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.DemoApp.Companion.GSON
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.liveclasssdkdemo.SignHelper
import cn.plaso.liveclasssdkdemo.config.MiniLessonSettings
import cn.plaso.liveclasssdkdemo.forceLandscapeOnPhone
import cn.plaso.upime.*
import org.json.JSONObject
import java.io.File

class MiniLessonBrowserActivity : AppCompatActivity() {
    companion object {
        val TAG = "MiniLesson"
    }

    lateinit var adapter: MiniLessonListAdapter
    lateinit var miniLessonViewModel: MiniLessonViewModel
    var supportUndo : Boolean = false
    var supportSaveDraft :Boolean = true
    private var teachToolTypes : Int = 0
    private var enablePptInteract = false
    private var openFileMode = UpimeConfig.OPEN_FILE_MODE_WINDOW
    private var supportHighlighter = false
    private var supportLocalFile = false
    private var toolBoxItems = UpimeConfig.ToolBoxItem.ALL.value
    private var recordType = MiniLessonSettings.RECORD_TYPE_AUDIO
    private var editElementMode: Int = 0  // 点擦为0， 对象擦：1（手写），3（手写+文本框），5（手写+图形），7（手写+文本框+图形）

    private lateinit var btnCreateMiniLesson: Button
    private lateinit var rvMiniLessonList: RecyclerView

    private lateinit var undo_switch: SwitchCompat
    private lateinit var save_draft_switch: SwitchCompat
    private lateinit var interact_ppt: SwitchCompat
    private lateinit var swNewPptMini: SwitchCompat
    private lateinit var rgOpenFileModeMini: RadioGroup
    private lateinit var swHighlighterMini: SwitchCompat
    private lateinit var swLocalFileMini: SwitchCompat
    private lateinit var recordtype: RadioGroup
    private lateinit var config_key: EditText
    private lateinit var mini_title: EditText
    private val teachToolIds = arrayOf(
        R.id.line_key, R.id.dashline_key, R.id.triangle_key,
        R.id.rect_key, R.id.ellipse_key, R.id.square_key,
        R.id.circle_key, R.id.arror_key, R.id.fanshaped_key, R.id.parallelogram_key
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceLandscapeOnPhone()
        setContentView(R.layout.activity_mini_lesson_browser)
        toolBoxItems = intent.getIntExtra("ToolBoxItem", UpimeConfig.ToolBoxItem.ALL.value)

        miniLessonViewModel = ViewModelProvider(this).get(MiniLessonViewModel::class.java)
        miniLessonViewModel.lessonList.observe(this, Observer {
            adapter.lessonList = it
            adapter.notifyDataSetChanged()
        })
        btnCreateMiniLesson = findViewById(R.id.btnCreateMiniLesson)
        btnCreateMiniLesson.setOnClickListener {
            startMiniLesson(null)
        }
        adapter = MiniLessonListAdapter().also {
            it.onDelete = {
                miniLessonViewModel.deleteLesson(it)
            }
            it.onEdit = {
                edit(it)
            }
            it.onPreview = {
                preview(it)
            }

            it.onUpload = {
                upload(it)
            }
        }

        rvMiniLessonList = findViewById(R.id.rvMiniLessonList)
        rvMiniLessonList.layoutManager = GridLayoutManager(this, 3)
        rvMiniLessonList.adapter = adapter
        (rvMiniLessonList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        teachToolIds.forEach { id ->
            findViewById<CheckBox>(id).setOnCheckedChangeListener { button, isChecked ->
                onTeachToolChecked(button.id, isChecked)
            }
        }

       undo_switch = findViewById(R.id.undo_switch)
       undo_switch.setOnCheckedChangeListener { btn, isChecked ->
           supportUndo = isChecked
           MiniLessonSettings.undoSupport = isChecked
       }

        save_draft_switch = findViewById(R.id.save_draft_switch)
       save_draft_switch.setOnCheckedChangeListener { btn, isChecked ->
           supportSaveDraft = isChecked
           MiniLessonSettings.supportSaveDraft = isChecked
       }

        interact_ppt = findViewById(R.id.interact_ppt)
        interact_ppt.setOnCheckedChangeListener { btn, isChecked ->
            enablePptInteract = isChecked
            MiniLessonSettings.enablePptInteract = isChecked
        }
        swNewPptMini = findViewById(R.id.sc_new_ppt)
        swNewPptMini.setOnCheckedChangeListener { _, isChecked ->
            MiniLessonSettings.useDxPptParse = isChecked
            DemoApp.newPPt = isChecked
        }
        rgOpenFileModeMini = findViewById(R.id.rgOpenFileModeMini)
        swHighlighterMini = findViewById(R.id.swHighlighterMini)
        swLocalFileMini = findViewById(R.id.swLocalFileMini)
        recordtype = findViewById(R.id.recordtype)
        config_key = findViewById(R.id.config_key)
        mini_title = findViewById(R.id.mini_title)
        
        loadConfigs()
        
        rgOpenFileModeMini.setOnCheckedChangeListener { _, checkedId ->
            openFileMode = if (checkedId == R.id.rbWindowMini) {
                UpimeConfig.OPEN_FILE_MODE_WINDOW
            } else {
                UpimeConfig.OPEN_FILE_MODE_IMAGE
            }
            MiniLessonSettings.openFileMode = openFileMode
        }
        swHighlighterMini.setOnCheckedChangeListener { _, isChecked ->
            supportHighlighter = isChecked
            MiniLessonSettings.supportHighlighter = isChecked
        }
        swLocalFileMini.setOnCheckedChangeListener { _, isChecked ->
            supportLocalFile = isChecked
            MiniLessonSettings.localFileSupport = isChecked
        }

        recordtype.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAudioType -> {
                    recordType = MiniLessonSettings.RECORD_TYPE_AUDIO
                }
                R.id.rbVideoType -> {
                    recordType = MiniLessonSettings.RECORD_TYPE_VIDEO
                }

            }
            MiniLessonSettings.recordType = recordType
        }
        config_key.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val configKey = config_key.text.toString()
                if (!TextUtils.isEmpty(configKey)) {
                    miniLessonViewModel.configKey = configKey
                    MiniLessonSettings.keyForColor = configKey
                }
            }
        });

        findViewById<Spinner>(R.id.sp_edit_element_mode).onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    editElementMode = resources.getStringArray(R.array.edit_element_mode_values)[position].toInt()
                    MiniLessonSettings.editElementMode = editElementMode
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadConfigs() {
        supportUndo = MiniLessonSettings.undoSupport
        undo_switch.isChecked = supportUndo

        supportSaveDraft = MiniLessonSettings.supportSaveDraft
        save_draft_switch.isChecked = supportSaveDraft

        enablePptInteract = MiniLessonSettings.enablePptInteract
        interact_ppt.isChecked = enablePptInteract
        swNewPptMini.isChecked = MiniLessonSettings.useDxPptParse
        DemoApp.newPPt = MiniLessonSettings.useDxPptParse

        openFileMode = MiniLessonSettings.openFileMode
        if (openFileMode == UpimeConfig.OPEN_FILE_MODE_WINDOW) {
            rgOpenFileModeMini.check(R.id.rbWindowMini)
        } else {
            rgOpenFileModeMini.check(R.id.rbImageMini)
        }

        supportHighlighter = MiniLessonSettings.supportHighlighter
        swHighlighterMini.isChecked = supportHighlighter

        supportLocalFile = MiniLessonSettings.localFileSupport
        swLocalFileMini.isChecked = supportLocalFile

        recordType = MiniLessonSettings.recordType
        if (recordType == MiniLessonSettings.RECORD_TYPE_AUDIO) {
            recordtype.check(R.id.rbAudioType)
        } else {
            recordtype.check(R.id.rbVideoType)
        }

        val configKey = MiniLessonSettings.keyForColor
        config_key.setText(configKey)
        miniLessonViewModel.configKey = configKey

        editElementMode = MiniLessonSettings.editElementMode
        val editModes = resources.getStringArray(R.array.edit_element_mode_values)
        findViewById<Spinner>(R.id.sp_edit_element_mode).setSelection(editModes.indexOf(editElementMode.toString()).coerceAtLeast(0))

        teachToolTypes = MiniLessonSettings.teachToolTypes
        applyTeachToolMask(teachToolTypes)
    }

    override fun onResume() {
        super.onResume()
        miniLessonViewModel.getMiniLessonList()
    }

    private fun startMiniLesson(lessonInfoWrap: LessonInfoWrap?) {
        val config = MiniLessonConfig()
        if (lessonInfoWrap == null) {
            if (!TextUtils.isEmpty(mini_title.text.toString())) {
                config.topic = mini_title.text.toString()
            }
            config.draftPath = "${getExternalFilesDir(null)?.absolutePath}/mini/${System.currentTimeMillis()}"
        } else {
            config.topic = lessonInfoWrap.lessonInfo.topic
            config.draftPath = lessonInfoWrap.lessonInfo.path
        }

        File(config.draftPath).also {
            if (!it.exists()) it.mkdirs()
        }

        config.supportDraft = supportSaveDraft
        config.supportUndo = supportUndo
        config.teachToolTypes = teachToolTypes or requiredTeachToolTypes()
        config.openFileMode = openFileMode
        config.recordType = recordType
        config.enableInteractPpt = enablePptInteract
        config.pptType = if (MiniLessonSettings.useDxPptParse) UpimeObject.TYPE_DX_PPT else UpimeObject.TYPE_PPT
        config.keyForColor = miniLessonViewModel.configKey
        config.forbiddenRecordScreen = true
        config.supportHighlighter = supportHighlighter
        config.supportLocalFile = supportLocalFile
        config.litMode = editElementMode

        MiniLessonSettings.teachToolTypes = teachToolTypes

        var extraJson = JSONObject()
        extraJson.put("id", "weikebao_id1")
        config.extra = extraJson.toString()

        DemoApp.upime.launchMiniLesson(config, object : IMiniLessonListener {
            override fun onMiniLessonReady(upimeBoard: UpimeBoard?) { DemoApp.upimeBoard = upimeBoard }
            override fun onClosed() = Unit
            override fun onDraftSaved(draft: LessonInfo?) {
                draft?.let { Log.d(TAG, "onDraftSaved: ${it}\ndraftPath${config.draftPath}") }
            }
            override fun onFinished(info: LessonInfo?) {
                info?.let { Log.d(TAG, "onFinished: ${it}\ndraftPath: ${config.draftPath}") }
                DemoApp.upimeBoard?.exit("close")
            }
            override fun onSkinChanged(skinId: Int) = Unit
        })
    }

    private fun upload(lessonInfoWrap: LessonInfoWrap) {
        val ossParams = OssTokenHelper.parseOssParams(GSON)
        if (ossParams == null) {
            showOssTokenInputDialog {
                upload(lessonInfoWrap)
            }
            return
        }
        doUpload(lessonInfoWrap, ossParams)
    }

    private fun showOssTokenInputDialog(onConfirmed: () -> Unit) {
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics
        ).toInt()
        val maxContentHeight = (resources.displayMetrics.heightPixels * 0.4f).toInt()

        val messageView = TextView(this).apply {
            text = getString(R.string.oss_token_input_message)
            setPadding(padding, 0, padding, padding)
        }

        val editText = EditText(this).apply {
            hint = getString(R.string.oss_token_input_hint)
            minLines = 4
            maxLines = 8
            isVerticalScrollBarEnabled = true
            setHorizontallyScrolling(false)
            setText(MiniLessonSettings.ossToken)
            setPadding(padding, padding, padding, padding)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(messageView)
            addView(
                editText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val scrollView = ScrollView(this).apply {
            addView(
                contentLayout,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
        scrollView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            maxContentHeight
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.oss_token_input_title)
            .setView(scrollView)
            .setPositiveButton(R.string.oss_token_confirm, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val input = editText.text.toString().trim()
                if (input.isEmpty()) {
                    Toast.makeText(this, R.string.oss_token_input_empty, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!OssTokenHelper.isValidTokenJson(GSON, input)) {
                    Toast.makeText(this, R.string.oss_token_input_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                MiniLessonSettings.ossToken = input
                dialog.dismiss()
                onConfirmed()
            }
        }
        dialog.show()
    }

    private fun doUpload(lessonInfoWrap: LessonInfoWrap, ossParams: OSSParams) {
        val params = UploadMLParams().also {
            it.localPath = lessonInfoWrap.localPath
            it.recordId = lessonInfoWrap.lessonInfo.recordId
            it.signedQuery = mutableMapOf<String, Any>().also { map ->
                map[UploadMLParams.QUERY_RECORD_ID] = it.recordId
                map[UploadMLParams.QUERY_APP_ID] = Config.appId
                map[UploadMLParams.QUERY_OP] = UploadMLParams.OP_UPLOAD_MINI
                map[UploadMLParams.QUERY_VALID_BEGIN] = System.currentTimeMillis() / 1000
                map[UploadMLParams.QUERY_VALID_TIME] = 120
                SignHelper.sign(map, Config.appKey)
            }
            it.ossParams = ossParams
        }
        DemoApp.upime.uploadMiniLesson(params, object : IUploadListener {
            override fun onUploadProgress(recordId: String?, progress: Int) {
                println("onUploadProgress $recordId $progress")
                adapter.updateProgress(recordId, progress)
            }
            override fun onUploadFinished(recordId: String?, resultCode: Int, lesson: LessonInfo?) {
                println("onUploadFinished $recordId $resultCode $lesson")
                miniLessonViewModel.uploadFinish(recordId, lesson, params.localPath)
            }
        })
    }

    private fun preview(it: LessonInfoWrap) {
        DemoApp.upime.playMiniLesson(it.lessonInfo.path, object : ILessonProvider {
            override fun getFileName(name: String?): String {
                Log.d(TAG, "getFileName: $name")
                return "$name"
            }
        })
    }

    private fun edit(it: LessonInfoWrap) {
        startMiniLesson(it)
    }

    private fun onTeachToolChecked(id: Int, isChecked: Boolean) {
        if (isRequiredTeachTool(id) && !isChecked) {
            return
        }
        val type = toolTypeFor(id)
        type?.let {
            teachToolTypes = if (isChecked) teachToolTypes or it.value else teachToolTypes and it.value.inv()
            teachToolTypes = teachToolTypes or requiredTeachToolTypes()
            MiniLessonSettings.teachToolTypes = teachToolTypes
        }
    }

    private fun toolTypeFor(id: Int): UpimeConfig.TeacherToolType? {
        return when (id) {
            R.id.triangle_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeTRIANGLE
            R.id.rect_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeRECT
            R.id.ellipse_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeELLIPSE
            R.id.line_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeLINE
            R.id.dashline_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE
            R.id.square_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeSQUARE
            R.id.circle_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeCIRCLE
            R.id.arror_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeARROR
            R.id.fanshaped_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeFANSHAPED
            R.id.parallelogram_key -> UpimeConfig.TeacherToolType.PureUpimeTeachToolTypePARALLELOGRAM
            else -> null
        }
    }

    private fun applyTeachToolMask(mask: Int) {
        val finalMask = mask or requiredTeachToolTypes()
        teachToolTypes = finalMask
        teachToolIds.forEach { id ->
            val checkBox = findViewById<CheckBox>(id)
            checkBox.setOnCheckedChangeListener(null)
            val toolType = toolTypeFor(id)
            checkBox.isChecked = toolType != null && (finalMask and toolType.value) != 0
            checkBox.isEnabled = !isRequiredTeachTool(id)
            checkBox.setOnCheckedChangeListener { button, isChecked ->
                onTeachToolChecked(button.id, isChecked)
            }
        }
    }

    private fun requiredTeachToolTypes(): Int {
        return UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeLINE.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeTRIANGLE.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeRECT.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeELLIPSE.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeSQUARE.value or
            UpimeConfig.TeacherToolType.PureUpimeTeachToolTypeCIRCLE.value
    }

    private fun isRequiredTeachTool(id: Int): Boolean {
        return id == R.id.line_key ||
            id == R.id.dashline_key ||
            id == R.id.triangle_key ||
            id == R.id.rect_key ||
            id == R.id.ellipse_key ||
            id == R.id.square_key ||
            id == R.id.circle_key
    }
}
