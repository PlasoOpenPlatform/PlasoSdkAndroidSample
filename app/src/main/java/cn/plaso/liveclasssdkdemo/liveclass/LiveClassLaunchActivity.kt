package cn.plaso.liveclasssdkdemo.liveclass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import cn.plaso.data.LiveClassConfig.Companion.VD_1080P
import cn.plaso.data.LiveClassConfig.Companion.VD_360P
import cn.plaso.data.LiveClassConfig.Companion.VD_720P
import cn.plaso.liveclasssdkdemo.AdvancedSettingsActivity
import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.liveclasssdkdemo.SecretFeatureState
import cn.plaso.liveclasssdkdemo.SecretTapReveal
import cn.plaso.liveclasssdkdemo.SignHelper
import cn.plaso.liveclasssdkdemo.config.AdvancedConfig
import cn.plaso.liveclasssdkdemo.config.LiveClassSettings
import cn.plaso.liveclasssdkdemo.forceLandscapeOnPhone
import cn.plaso.upime.ClassConfig
import cn.plaso.upime.ClassConfig.CAST
import cn.plaso.upime.ClassConfig.QIANGDAQI
import cn.plaso.upime.ClassConfig.RED_PACKET
import cn.plaso.upime.ClassConfig.SMALL_BOARD
import cn.plaso.upime.ClassConfig.TOUZI
import cn.plaso.upime.ClassConfig.VIDEO_MARK
import cn.plaso.upime.IBoardCallback
import cn.plaso.upime.ILiveClassListener
import cn.plaso.upime.UpimeBoard
import cn.plaso.upime.UpimeBoardData
import cn.plaso.upime.UpimeConfig
import cn.plaso.upime.UpimeObject
import cn.plaso.upime.UpimeParameter

class LiveClassLaunchActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private val TAG = LiveClassLaunchActivity::class.java.simpleName
    private val advancedSettingsReveal = SecretTapReveal()

    private var teachToolTypes: Int = 0
    private var toolBoxItems: Int = 0
    private lateinit var etMeetingId: EditText
    private lateinit var etName: EditText
    private lateinit var etClassName: EditText
    private lateinit var etOnlineMode: EditText
    private lateinit var etColor: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etRedPacketLimit: EditText
    private lateinit var spEditModeBasic: Spinner
    private lateinit var rgUserType: RadioGroup
    private lateinit var rgMeetingType: RadioGroup
    private lateinit var rgVideoDefinition: RadioGroup
    private lateinit var rgOpenFileModeLive: RadioGroup
    private lateinit var swResidentCam: SwitchCompat
    private lateinit var swLowLatencyBasic: SwitchCompat
    private lateinit var swHighlighterLive: SwitchCompat
    private lateinit var swLocalFileLive: SwitchCompat
    private lateinit var swPptInteract: SwitchCompat
    private lateinit var swNewPpt: SwitchCompat
    private lateinit var swUndo: SwitchCompat
    private lateinit var swNewSmallBoard: SwitchCompat
    private lateinit var spNewQuizMode: Spinner
    private lateinit var swEnableStarShow: SwitchCompat
    private lateinit var swHideRecordingIndicator: SwitchCompat
    private lateinit var swShowStudentClassElapsedTime: SwitchCompat
    private lateinit var swSaveBoard: SwitchCompat
    private lateinit var swSelect: SwitchCompat
    private lateinit var cbSignIn: CheckBox
    private lateinit var cbVote: CheckBox

    private val teachToolIds = arrayOf(
        R.id.line_key, R.id.dashline_key, R.id.triangle_key,
        R.id.rect_key, R.id.ellipse_key, R.id.square_key,
        R.id.circle_key, R.id.arror_key, R.id.fanshaped_key, R.id.parallelogram_key
    )
    private val toolBoxIds = arrayOf(
        R.id.class_test, R.id.timer_count, R.id.small_board,
        R.id.red_packet, R.id.browser, R.id.responder, R.id.dice,
        R.id.sc_signin, R.id.sc_vote
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceLandscapeOnPhone()
        setContentView(R.layout.activity_liveclass_launch)

        initViews()
        loadConfigs()
        setupListeners()
    }

    private fun initViews() {
        etMeetingId = findViewById(R.id.etMeetingId)
        etName = findViewById(R.id.etName)
        etClassName = findViewById(R.id.etClassName)
        etOnlineMode = findViewById(R.id.etOnlineMode)
        etColor = findViewById(R.id.etColor)
        etEndTime = findViewById(R.id.endTime)
        etRedPacketLimit = findViewById(R.id.redPacketLimit)
        spEditModeBasic = findViewById(R.id.spEditModeBasic)
        rgUserType = findViewById(R.id.rgUserType)
        rgMeetingType = findViewById(R.id.rgMeetingType)
        rgVideoDefinition = findViewById(R.id.rgVideoDefinition)
        rgOpenFileModeLive = findViewById(R.id.rgOpenFileModeLive)
        swResidentCam = findViewById(R.id.resident_camera)
        swLowLatencyBasic = findViewById(R.id.swLowLatencyBasic)
        swHighlighterLive = findViewById(R.id.swHighlighterLive)
        swLocalFileLive = findViewById(R.id.swLocalFileLive)
        swPptInteract = findViewById(R.id.ppt_interact)
        swNewPpt = findViewById(R.id.sc_new_ppt)
        swUndo = findViewById(R.id.undo_support_switch)
        swNewSmallBoard = findViewById(R.id.new_smallBoard)
        spNewQuizMode = findViewById(R.id.spNewQuizMode)
        swEnableStarShow = findViewById(R.id.sc_enable_star_show)
        swHideRecordingIndicator = findViewById(R.id.sc_hide_recording_indicator)
        swShowStudentClassElapsedTime = findViewById(R.id.sc_show_student_class_elapsed_time)
        swSaveBoard = findViewById(R.id.sc_save_board)
        swSelect = findViewById(R.id.selectSwitch)
        cbSignIn = findViewById(R.id.sc_signin)
        cbVote = findViewById(R.id.sc_vote)
        toolBoxIds.forEach { findViewById<CheckBox>(it).setOnCheckedChangeListener(this) }

        val btnAdvanced = findViewById<Button>(R.id.btnAdvanced)
        btnAdvanced.visibility = if (SecretFeatureState.advancedSettingsRevealed) VISIBLE else GONE
        btnAdvanced.setOnClickListener {
            startActivity(Intent(this, AdvancedSettingsActivity::class.java))
        }

        findViewById<TextView>(R.id.tvLiveClassTitle).setOnClickListener {
            advancedSettingsReveal.onTap {
                SecretFeatureState.advancedSettingsRevealed = true
                btnAdvanced.visibility = VISIBLE
            }
        }
    }

    private fun loadConfigs() {
        etMeetingId.setText(DemoApp.sp.getString("last_meeting_id", ""))
        etName.setText(DemoApp.sp.getString("last_user_name", ""))
        if (TextUtils.isEmpty(etClassName.text)) {
            etClassName.setText(R.string.default_class_name)
        }
        etOnlineMode.setText(AdvancedConfig.onlineMode.toString())
        etColor.setText(AdvancedConfig.keyForColor)
        etEndTime.setText("")
        etRedPacketLimit.setText(AdvancedConfig.redPacketLimit.toString())

        when (LiveClassSettings.userType) {
            "speaker" -> rgUserType.check(R.id.rbSpeaker)
            "assistant" -> rgUserType.check(R.id.rbAssistant)
            "visitor" -> rgUserType.check(R.id.rbVisitor)
            else -> rgUserType.check(R.id.rbListener)
        }

        if (LiveClassSettings.meetingType == "video") {
            rgMeetingType.check(R.id.rbVideo)
        } else {
            rgMeetingType.check(R.id.rbAudio)
        }

        when (LiveClassSettings.videoDefinition) {
            VD_1080P -> rgVideoDefinition.check(R.id.rbVD1080)
            VD_720P -> rgVideoDefinition.check(R.id.rbVD720)
            else -> rgVideoDefinition.check(R.id.rbVD360)
        }

        if (!SecretFeatureState.advancedSettingsRevealed) {
            AdvancedConfig.resolution = "1280x720"
        }
        if (LiveClassSettings.openFileMode == UpimeConfig.OPEN_FILE_MODE_WINDOW) {
            rgOpenFileModeLive.check(R.id.rbWindowLive)
        } else {
            rgOpenFileModeLive.check(R.id.rbImageLive)
        }

        swResidentCam.isChecked = LiveClassSettings.isResidentCamera
        swLowLatencyBasic.isChecked = AdvancedConfig.lowLatencyClass
        swHighlighterLive.isChecked = LiveClassSettings.supportHighlighter
        swLocalFileLive.isChecked = LiveClassSettings.localFileSupport
        swPptInteract.isChecked = LiveClassSettings.enablePptInteract
        swNewPpt.isChecked = LiveClassSettings.useDxPptParse
        DemoApp.newPPt = LiveClassSettings.useDxPptParse
        swUndo.isChecked = LiveClassSettings.undoSupport
        swNewSmallBoard.isChecked = LiveClassSettings.useNewSmallBoard
        spNewQuizMode.setSelection(AdvancedConfig.newQuizMode.coerceIn(0, 3))
        swEnableStarShow.isChecked = AdvancedConfig.enableStarShow
        swHideRecordingIndicator.isChecked = AdvancedConfig.hideRecordingIndicator
        swShowStudentClassElapsedTime.isChecked = AdvancedConfig.showStudentClassElapsedTime
        swSaveBoard.isChecked = LiveClassSettings.enableSaveBoard
        swSelect.isChecked = LiveClassSettings.supportSelect
        cbSignIn.isChecked = LiveClassSettings.enableLiveSign
        cbVote.isChecked = LiveClassSettings.enableVote
        if (LiveClassSettings.hasToolBoxItems()) {
            applyToolBoxMask(LiveClassSettings.toolBoxItems)
        } else {
            syncToolBoxItemsFromViews()
        }
        val editModes = resources.getStringArray(R.array.edit_element_mode_values)
        spEditModeBasic.setSelection(editModes.indexOf(LiveClassSettings.editElementMode.toString()).coerceAtLeast(0))

        teachToolTypes = LiveClassSettings.teachToolTypes
        applyTeachToolMask(teachToolTypes)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnEnterLiveClass).setOnClickListener { launchLiveClass() }
    }

    private fun saveCurrentConfigs() {
        val userRole = when (rgUserType.checkedRadioButtonId) {
            R.id.rbSpeaker -> "speaker"
            R.id.rbAssistant -> "assistant"
            R.id.rbVisitor -> "visitor"
            else -> "listener"
        }
        LiveClassSettings.userType = userRole
        LiveClassSettings.meetingType = if (rgMeetingType.checkedRadioButtonId == R.id.rbVideo) "video" else "audio"
        LiveClassSettings.videoDefinition = when (rgVideoDefinition.checkedRadioButtonId) {
            R.id.rbVD1080 -> VD_1080P
            R.id.rbVD720 -> VD_720P
            else -> VD_360P
        }
        LiveClassSettings.openFileMode = if (rgOpenFileModeLive.checkedRadioButtonId == R.id.rbWindowLive) {
            UpimeConfig.OPEN_FILE_MODE_WINDOW
        } else {
            UpimeConfig.OPEN_FILE_MODE_IMAGE
        }
        AdvancedConfig.onlineMode = etOnlineMode.text.toString().toIntOrNull() ?: 1
        AdvancedConfig.keyForColor = etColor.text.toString()
        AdvancedConfig.redPacketLimit = etRedPacketLimit.text.toString().toIntOrNull() ?: 0
        LiveClassSettings.isResidentCamera = swResidentCam.isChecked
        AdvancedConfig.lowLatencyClass = swLowLatencyBasic.isChecked
        LiveClassSettings.supportHighlighter = swHighlighterLive.isChecked
        LiveClassSettings.localFileSupport = swLocalFileLive.isChecked
        LiveClassSettings.enablePptInteract = swPptInteract.isChecked
        LiveClassSettings.useDxPptParse = swNewPpt.isChecked
        DemoApp.newPPt = swNewPpt.isChecked
        LiveClassSettings.undoSupport = swUndo.isChecked
        LiveClassSettings.useNewSmallBoard = swNewSmallBoard.isChecked
        AdvancedConfig.newQuizMode = spNewQuizMode.selectedItemPosition
        AdvancedConfig.enableStarShow = swEnableStarShow.isChecked
        AdvancedConfig.hideRecordingIndicator = swHideRecordingIndicator.isChecked
        AdvancedConfig.showStudentClassElapsedTime = swShowStudentClassElapsedTime.isChecked
        LiveClassSettings.enableSaveBoard = swSaveBoard.isChecked
        LiveClassSettings.supportSelect = swSelect.isChecked
        LiveClassSettings.enableLiveSign = cbSignIn.isChecked
        LiveClassSettings.enableVote = cbVote.isChecked
        LiveClassSettings.teachToolTypes = teachToolTypes
        syncToolBoxItemsFromViews()
        val editModes = resources.getStringArray(R.array.edit_element_mode_values)
        LiveClassSettings.editElementMode = editModes[spEditModeBasic.selectedItemPosition].toInt()

        DemoApp.sp.edit {
            putString("last_meeting_id", etMeetingId.text.toString())
                .putString("last_user_name", etName.text.toString())
        }
    }

    private fun launchLiveClass() {
        val meetingId = etMeetingId.text.toString()
        val userName = etName.text.toString()

        if (TextUtils.isEmpty(meetingId)) {
            etMeetingId.error = getString(R.string.enter_meeting_number)
            return
        }
        if (TextUtils.isEmpty(userName)) {
            etName.error = getString(R.string.user_name)
            return
        }

        saveCurrentConfigs()

        // SDK 参数配置
        val parameter = UpimeParameter().apply {
            waterMark = "liveclassdemo"
            waterMarkDynamic = true
            waterMarkOpacity = 0.8
            waterMarkSize = 32.0
            logLevel = AdvancedConfig.logLevel
            appId = Config.appId
            logDir = getExternalFilesDir("logs")!!.path
        }
        DemoApp.upime.upimeParameter = parameter

        val query = getQuery(meetingId, userName)
        val normalizedToolBoxItems = toolBoxItems
        val featureMask = buildFeatureMask(normalizedToolBoxItems)
        val config = ClassConfig().apply {
            classURL = if (TextUtils.isEmpty(AdvancedConfig.hostAddr)) query else "${AdvancedConfig.hostAddr}?$query"
            host = Config.server
            features = featureMask
            keyForColor = AdvancedConfig.keyForColor
            redPacketLimit = AdvancedConfig.redPacketLimit
            endRemindTime = AdvancedConfig.endRemindTime
            openFileMode = LiveClassSettings.openFileMode
            toolboxItems = normalizedToolBoxItems
            enableInteractPpt = LiveClassSettings.enablePptInteract
            teachToolTypes = this@LiveClassLaunchActivity.teachToolTypes or requiredTeachToolTypes()
            supportUndo = LiveClassSettings.undoSupport
            useNewSmallBoard = LiveClassSettings.useNewSmallBoard
            supportSelect = LiveClassSettings.supportSelect
            residentCamera = LiveClassSettings.isResidentCamera
            mobileTeaching = AdvancedConfig.mobileTeaching
            auxiliaryCamera = AdvancedConfig.auxiliaryCamera
            pptType = if (LiveClassSettings.useDxPptParse) UpimeObject.TYPE_DX_PPT else UpimeObject.TYPE_PPT
            enableUploadLog = AdvancedConfig.enableUploadLog
            enableRearCamera = AdvancedConfig.enableRearCamera
            supportHighlighter = LiveClassSettings.supportHighlighter
            supportLocalFile = LiveClassSettings.localFileSupport
            enableSaveBoard = LiveClassSettings.enableSaveBoard
            enableStarShow = AdvancedConfig.enableStarShow
            enableRecallMsg = AdvancedConfig.enableRecallMsg
            enableLiveclassSignRollcall = AdvancedConfig.enableLiveclassSignRollcall
            enableVote = LiveClassSettings.enableVote
            enableLiveSign = LiveClassSettings.enableLiveSign
            enableLiveOfflineSign = AdvancedConfig.enableLiveOfflineSign
            hideRecordingIndicator = AdvancedConfig.hideRecordingIndicator
            showStudentClassElapsedTime = AdvancedConfig.showStudentClassElapsedTime
            if (AdvancedConfig.useMeetingMode) {
                defaultPermission = AdvancedConfig.meetingDefaultPermission
            }
        }

        if (AdvancedConfig.enableUploadLog) {
            DemoApp.upime.addUploadLogListener { reason ->
                Log.d(TAG, "onReportIssues reason: $reason")
                true
            }
        }

        DemoApp.upime.launchLiveClass(config, object : ILiveClassListener {
            override fun onLiveClassReady(upimeBoard: UpimeBoard?) { DemoApp.upimeBoard = upimeBoard }
            override fun onExited(exitCode: Int, meetingId: String) {}
            override fun onSkinChanged(skinId: Int) {}
            override fun onUpimeBoardSaved(p0: Context?, p1: UpimeBoardData?, p2: IBoardCallback?) { p2?.onSaveSuccess() }
        })
    }

    private fun getQuery(meetingId: String, userName: String): String {
        val params = mutableMapOf<String, Any>().also {
            it["appId"] = Config.appId
            it["appType"] = "liveclassSDK"
            it["beginTime"] = System.currentTimeMillis() / 1000
            it["mediaType"] = LiveClassSettings.meetingType
            it["meetingId"] = meetingId
            it["meetingType"] = if (AdvancedConfig.useMeetingMode) "meeting" else "public"
            it["loginName"] = userName
            it["userName"] = userName
            it["userType"] = LiveClassSettings.userType
            it["validTime"] = 1080000
            it["onlineMode"] = AdvancedConfig.onlineMode
            val endTimeValue = etEndTime.text?.toString()?.trim()?.toLongOrNull()
            if (endTimeValue != null && endTimeValue > 0) {
                it["endTime"] = endTimeValue
            }
            it["d_sharpness"] = LiveClassSettings.videoDefinition
            it["d_dimension"] = if (SecretFeatureState.advancedSettingsRevealed) {
                AdvancedConfig.resolution
            } else {
                "1280x720"
            }
            if (AdvancedConfig.liveStreaming) {
                it["videoStream"] = 6
                it["classOption"] = 16
            }
            it["vendorType"] = AdvancedConfig.rtcType
            it["enableNewClassExam"] = AdvancedConfig.newQuizMode
            it["d_enableObjectEraser"] = LiveClassSettings.editElementMode
            it["d_lowLatencyClass"] = if (AdvancedConfig.lowLatencyClass) 1 else 0
            it["topic"] = etClassName.text?.toString().orEmpty()
        }
        return SignHelper.sign(params, Config.appKey)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val viewId = buttonView?.id ?: return
        if (isRequiredTeachTool(viewId) && !isChecked) {
            return
        }
        val teachToolType = toolTypeFor(viewId)
        if (teachToolType != null) {
            teachToolTypes = if (isChecked) teachToolTypes or teachToolType.value else teachToolTypes and teachToolType.value.inv()
            teachToolTypes = teachToolTypes or requiredTeachToolTypes()
            LiveClassSettings.teachToolTypes = teachToolTypes
            return
        }
        if (viewId == R.id.sc_signin) {
            LiveClassSettings.enableLiveSign = isChecked
            return
        }
        if (viewId == R.id.sc_vote) {
            LiveClassSettings.enableVote = isChecked
            return
        }
        if (toolBoxItemFor(viewId) != null) {
            syncToolBoxItemsFromViews()
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

    private fun toolBoxItemFor(id: Int): UpimeConfig.ToolBoxItem? {
        return when (id) {
            R.id.class_test -> UpimeConfig.ToolBoxItem.CLASS_TEST
            R.id.timer_count -> UpimeConfig.ToolBoxItem.TIMER_COUNT
            R.id.small_board -> UpimeConfig.ToolBoxItem.SMALL_BOARD
            R.id.red_packet -> UpimeConfig.ToolBoxItem.RED_PACKET
            R.id.browser -> UpimeConfig.ToolBoxItem.BROWSER
            R.id.responder -> UpimeConfig.ToolBoxItem.RESPONDER
            R.id.dice -> UpimeConfig.ToolBoxItem.DICE
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

    private fun applyToolBoxMask(mask: Int) {
        toolBoxItems = mask
        toolBoxIds.forEach { id ->
            val checkBox = findViewById<CheckBox>(id)
            checkBox.setOnCheckedChangeListener(null)
            val toolItem = toolBoxItemFor(id)
            checkBox.isChecked = toolItem != null && (mask and toolItem.value) != 0
            checkBox.setOnCheckedChangeListener(this)
        }
    }

    private fun syncToolBoxItemsFromViews() {
        var mask = 0
        toolBoxIds.forEach { id ->
            val toolItem = toolBoxItemFor(id) ?: return@forEach
            if (findViewById<CheckBox>(id).isChecked) {
                mask = mask or toolItem.value
            }
        }
        toolBoxItems = mask
        LiveClassSettings.toolBoxItems = toolBoxItems
    }

    private fun buildFeatureMask(toolBoxMask: Int): Int {
        var mask = VIDEO_MARK or CAST
        if ((toolBoxMask and UpimeConfig.ToolBoxItem.SMALL_BOARD.value) != 0) {
            mask = mask or SMALL_BOARD
        }
        if ((toolBoxMask and UpimeConfig.ToolBoxItem.RED_PACKET.value) != 0) {
            mask = mask or RED_PACKET
        }
        if ((toolBoxMask and UpimeConfig.ToolBoxItem.RESPONDER.value) != 0) {
            mask = mask or QIANGDAQI
        }
        if ((toolBoxMask and UpimeConfig.ToolBoxItem.DICE.value) != 0) {
            mask = mask or TOUZI
        }
        return mask
    }
}
