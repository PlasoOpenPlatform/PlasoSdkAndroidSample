package cn.plaso.liveclasssdkdemo.preparelesson

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.liveclasssdkdemo.config.AdvancedConfig
import cn.plaso.liveclasssdkdemo.config.PrepareLessonSettings
import cn.plaso.liveclasssdkdemo.forceLandscapeOnPhone
import cn.plaso.upime.IBoardCallback
import cn.plaso.upime.IPrepareLessonListener
import cn.plaso.upime.UpimeBoard
import cn.plaso.upime.UpimeBoardData
import cn.plaso.upime.UpimeConfig
import cn.plaso.upime.UpimeObject
import cn.plaso.upime.UpimeParameter
import cn.plaso.upime.PrepareLessonConfig as UpimePrepareLessonConfig

class PrepareLessonActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private var teachToolTypes: Int = 0
    private lateinit var etName: EditText
    private lateinit var etColor: EditText
    private lateinit var rgUserType: RadioGroup
    private lateinit var rgOpenFileModePrepare: RadioGroup
    private lateinit var swResidentCam: SwitchCompat
    private lateinit var swPptInteract: SwitchCompat
    private lateinit var swNewPpt: SwitchCompat
    private lateinit var swUndo: SwitchCompat
    private lateinit var swSaveBoard: SwitchCompat
    private lateinit var swHighlighterPrepare: SwitchCompat
    private lateinit var swLocalFilePrepare: SwitchCompat
    private lateinit var swSelect: SwitchCompat
    private lateinit var spEditMode: Spinner

    private val teachToolIds = arrayOf(
        R.id.line_key, R.id.dashline_key, R.id.triangle_key,
        R.id.rect_key, R.id.ellipse_key, R.id.square_key,
        R.id.circle_key, R.id.arror_key, R.id.fanshaped_key, R.id.parallelogram_key
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceLandscapeOnPhone()
        setContentView(R.layout.activity_prepare_lesson)

        initViews()
        loadConfigs()
        setupListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etColor = findViewById(R.id.etColor)
        rgUserType = findViewById(R.id.rgUserType)
        rgOpenFileModePrepare = findViewById(R.id.rgOpenFileModePrepare)
        swResidentCam = findViewById(R.id.resident_camera)
        swPptInteract = findViewById(R.id.ppt_interact)
        swNewPpt = findViewById(R.id.sc_new_ppt)
        swUndo = findViewById(R.id.undo_support_switch)
        swSaveBoard = findViewById(R.id.sc_save_board)
        swHighlighterPrepare = findViewById(R.id.swHighlighterPrepare)
        swLocalFilePrepare = findViewById(R.id.swLocalFilePrepare)
        swSelect = findViewById(R.id.selectSwitch)
        spEditMode = findViewById(R.id.sp_edit_element_mode)
    }

    private fun loadConfigs() {
        etName.setText(DemoApp.sp.getString("last_prepare_name", ""))
        etColor.setText(PrepareLessonSettings.keyForColor)

        if (PrepareLessonSettings.userType == "assistant") {
            rgUserType.check(R.id.rbAssistant)
        } else {
            rgUserType.check(R.id.rbSpeaker)
        }

        swResidentCam.isChecked = PrepareLessonSettings.isResidentCamera
        swPptInteract.isChecked = PrepareLessonSettings.enablePptInteract
        swNewPpt.isChecked = PrepareLessonSettings.useDxPptParse
        DemoApp.newPPt = PrepareLessonSettings.useDxPptParse
        swUndo.isChecked = PrepareLessonSettings.undoSupport
        swSaveBoard.isChecked = PrepareLessonSettings.supportSaveBoard
        swHighlighterPrepare.isChecked = PrepareLessonSettings.supportHighlighter
        swLocalFilePrepare.isChecked = PrepareLessonSettings.localFileSupport
        swSelect.isChecked = PrepareLessonSettings.supportSelect
        if (PrepareLessonSettings.openFileMode == UpimeConfig.OPEN_FILE_MODE_WINDOW) {
            rgOpenFileModePrepare.check(R.id.rbWindowPrepare)
        } else {
            rgOpenFileModePrepare.check(R.id.rbImagePrepare)
        }

        val editModes = resources.getStringArray(R.array.edit_element_mode_values)
        spEditMode.setSelection(editModes.indexOf(PrepareLessonSettings.editElementMode.toString()).coerceAtLeast(0))

        teachToolTypes = PrepareLessonSettings.teachToolTypes
        applyTeachToolMask(teachToolTypes)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnPrepareLesson).setOnClickListener { launchPrepareLesson() }
    }

    private fun saveCurrentConfigs() {
        PrepareLessonSettings.userType = if (rgUserType.checkedRadioButtonId == R.id.rbAssistant) "assistant" else "speaker"
        PrepareLessonSettings.isResidentCamera = swResidentCam.isChecked
        PrepareLessonSettings.enablePptInteract = swPptInteract.isChecked
        PrepareLessonSettings.useDxPptParse = swNewPpt.isChecked
        DemoApp.newPPt = swNewPpt.isChecked
        PrepareLessonSettings.undoSupport = swUndo.isChecked
        PrepareLessonSettings.supportSaveBoard = swSaveBoard.isChecked
        PrepareLessonSettings.supportHighlighter = swHighlighterPrepare.isChecked
        PrepareLessonSettings.localFileSupport = swLocalFilePrepare.isChecked
        PrepareLessonSettings.supportSelect = swSelect.isChecked
        PrepareLessonSettings.openFileMode = if (rgOpenFileModePrepare.checkedRadioButtonId == R.id.rbWindowPrepare) {
            UpimeConfig.OPEN_FILE_MODE_WINDOW
        } else {
            UpimeConfig.OPEN_FILE_MODE_IMAGE
        }
        PrepareLessonSettings.teachToolTypes = teachToolTypes
        PrepareLessonSettings.keyForColor = etColor.text.toString()

        val editModes = resources.getStringArray(R.array.edit_element_mode_values)
        val selectedEditMode = editModes[spEditMode.selectedItemPosition].toInt()
        PrepareLessonSettings.editElementMode = selectedEditMode

        DemoApp.sp.edit { putString("last_prepare_name", etName.text.toString()) }
    }

    private fun launchPrepareLesson() {
        val userName = etName.text.toString()
        if (TextUtils.isEmpty(userName)) {
            etName.error = getString(R.string.prepare_enter_name)
            return
        }

        saveCurrentConfigs()

        val parameter = UpimeParameter().apply {
            waterMark = "liveclassdemo"
            logLevel = AdvancedConfig.logLevel
            appId = Config.appId
            logDir = getExternalFilesDir("logs")!!.path
        }
        DemoApp.upime.upimeParameter = parameter

        val config = UpimePrepareLessonConfig().apply {
            draftPath = externalCacheDir?.absolutePath
            this.userName = userName
            this.loginName = userName
            this.userType = PrepareLessonSettings.userType
            this.topic = getString(R.string.prepare_lesson)
            host = Config.server
            openFileMode = PrepareLessonSettings.openFileMode
            keyForColor = etColor.text.toString()
            enableInteractPpt = PrepareLessonSettings.enablePptInteract
            teachToolTypes = this@PrepareLessonActivity.teachToolTypes or requiredTeachToolTypes()
            supportUndo = PrepareLessonSettings.undoSupport
            supportSelect = PrepareLessonSettings.supportSelect
            residentCamera = PrepareLessonSettings.isResidentCamera
            pptType = if (PrepareLessonSettings.useDxPptParse) UpimeObject.TYPE_DX_PPT else UpimeObject.TYPE_PPT
            enableSaveBoard = PrepareLessonSettings.supportSaveBoard
            litMode = resources.getStringArray(R.array.edit_element_mode_values)[spEditMode.selectedItemPosition].toInt()
            supportHighlighter = PrepareLessonSettings.supportHighlighter
            supportLocalFile = PrepareLessonSettings.localFileSupport
        }

        DemoApp.upime.launchPrepareLesson(config, object : IPrepareLessonListener {
            override fun onPrepareLessonReady(upimeBoard: UpimeBoard?) { DemoApp.upimeBoard = upimeBoard }
            override fun onUpimeBoardSaved(p0: Context?, p1: UpimeBoardData?, p2: IBoardCallback?) { p2?.onSaveSuccess() }
            override fun onPrepareLessonExited() {}
        })
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val viewId = buttonView?.id ?: return
        if (isRequiredTeachTool(viewId) && !isChecked) {
            return
        }
        val type = toolTypeFor(viewId)
        type?.let {
            teachToolTypes = if (isChecked) teachToolTypes or it.value else teachToolTypes and it.value.inv()
            teachToolTypes = teachToolTypes or requiredTeachToolTypes()
            PrepareLessonSettings.teachToolTypes = teachToolTypes
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
            checkBox.setOnCheckedChangeListener(this)
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
