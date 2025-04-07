package cn.plaso.liveclasssdkdemo.liveclass

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.liveclasssdkdemo.SignHelper
import cn.plaso.upime.ClassConfig
import cn.plaso.upime.IBoardCallback
import cn.plaso.upime.ILiveClassListener
import cn.plaso.upime.UpimeBoard
import cn.plaso.upime.UpimeBoardData
import cn.plaso.upime.UpimeConfig
import cn.plaso.upime.UpimeParameter
import cn.plaso.upime.WebviewObject
import com.google.gson.Gson

class LiveClassLaunchActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var meetingType: String = "video"
    private var videoStream: Int = -1;
    private var userType: String = "listener"
    private var openFileMode = UpimeConfig.OPEN_FILE_MODE_WINDOW
    private var enableLiveSign = false;
    private var enableVote = false;
    private var enableBlueTooth = false
    private var enablePptInteract = false
    private var undoSupport = false
    private var useNewSmallBoard = false;
    private var useMeetingMode = false;
    private var supportSelect = true;
    private var teachToolTypes: Int = 0
    private var toolBoxItems: Int = 0
    private var rtcType: Int = 2
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var MEETING_ID: String = "meeting_id"
    private var LOGIN_NAME: String = "login_name"
    private var ONLINE_MODE: String = "online_mode"
    private var MEETING_TYPE: String = "meeting_type"
    private var VIDEO_STREAM: String = "video_stream"
    private var ROLE: String = "role"
    private var DIMISSION: String = "dimission"
    private var resolution = "1280x720"
    private var isPhoneTeachingMethod = false;
    private var isResidentCamera = true;
    private var isAuxiliaryCamera = true;
    private var isNewPpt = false;
    private var logLevel = UpimeParameter.INFO;
    private var teachingList: MutableList<WebviewObject> = ArrayList(16)
    private var supportNewQuiz: Boolean = false // 是否支持新版随堂测；
    private var enableSaveBoard: Boolean = false // 是否支持保存板书
    private var editElementMode: Int = 0  // 点擦为0， 对象擦：1（手写），3（手写+文本框），5（手写+图形），7（手写+文本框+图形）

    private lateinit var etMeetingId: EditText
    private lateinit var etName: EditText
    private lateinit var rbSpeaker: RadioButton
    private lateinit var rbAssistant: RadioButton
    private lateinit var etOnlineMode: EditText
    private lateinit var etColor: EditText
    private lateinit var rbListener: RadioButton
    private lateinit var rgVideoDimension: RadioGroup

    private var ids = arrayOf(
        R.id.triangle_key,
        R.id.rect_key,
        R.id.ellipse_key,
        R.id.line_key,
        R.id.dashline_key,
        R.id.square_key,
        R.id.circle_key,
        R.id.arror_key,
        R.id.fanshaped_key,
        R.id.parallelogram_key,
    )


    enum class TeacherToolType(val value: Int) {
        PureUpimeTeachToolTypeTRIANGLE(1 shl 0),
        PureUpimeTeachToolTypeRECT(1 shl 1),
        PureUpimeTeachToolTypeELLIPSE(1 shl 2),
        PureUpimeTeachToolTypeLINE(1 shl 3),
        PureUpimeTeachToolTypeDASHEDLINE(1 shl 4),
        PureUpimeTeachToolTypeSQUARE(1 shl 5),
        PureUpimeTeachToolTypeCIRCLE(1 shl 6),
        PureUpimeTeachToolTypeARROR(1 shl 7),
        PureUpimeTeachToolTypeFANSHAPED(1 shl 8),
        PureUpimeTeachToolTypePARALLELOGRAM(1 shl 9),
        PureUpimeTeachToolTypeAll(1023)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liveclass_launch)

        etName = findViewById(R.id.etName)
        rbSpeaker = findViewById(R.id.rbSpeaker)
        rbAssistant = findViewById(R.id.rbAssistant)
        etOnlineMode = findViewById(R.id.etOnlineMode)
        etColor = findViewById(R.id.etColor)
        rbListener = findViewById(R.id.rbListener)
        rgVideoDimension = findViewById(R.id.rgVideoDimension)
        etMeetingId= findViewById<EditText>(R.id.etMeetingId)

        sharedPreferences = this.getSharedPreferences("default", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()

        openFileMode = intent.getIntExtra("OpenFileMode", UpimeConfig.OPEN_FILE_MODE_IMAGE)
        toolBoxItems = intent.getIntExtra("ToolBoxItem", 0)
        enableLiveSign = intent.getBooleanExtra("SupportSignIn", false)
        enableVote = intent.getBooleanExtra("SupportVote", false)
        var meettype = sharedPreferences?.getString(MEETING_TYPE, "video")
        if ("video".equals(meettype)) {
            findViewById<RadioGroup>(R.id.rgMeetingType).check(R.id.rbVideo)
            meetingType = "video"
            videoStream = sharedPreferences?.getInt(VIDEO_STREAM, -1) ?: -1
            if (videoStream == 18) {
                findViewById<SwitchCompat>(R.id.switchVideoStream).isChecked = true;
            }
        } else {
            findViewById<RadioGroup>(R.id.rgMeetingType).check(R.id.rbAudio)
            meetingType = "audio"
        }

        findViewById<RadioGroup>(R.id.rgMeetingType).setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbVideo -> {
                    meetingType = "video"
                    findViewById<SwitchCompat>(R.id.switchVideoStream).visibility = VISIBLE
                }
                R.id.rbAudio -> {
                    meetingType = "audio"
                    findViewById<SwitchCompat>(R.id.switchVideoStream).isChecked = false
                    findViewById<SwitchCompat>(R.id.switchVideoStream).visibility = GONE
                }
            }
        }

        var usertype = sharedPreferences?.getString(ROLE, "listener")
        var rgUserType = findViewById<RadioGroup>(R.id.rgUserType)
        if ("speaker".equals(usertype)) {
            rgUserType.check(R.id.rbSpeaker)
            userType = "speaker"
        } else if ("assistant".equals(usertype)) {
            rgUserType.check(R.id.rbAssistant)
            userType = "assistant"
        } else if ("visitor".equals(usertype)) {
            rgUserType.check(R.id.rbVisitor)
            userType = "visitor"
        }

        rgUserType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbSpeaker -> userType = "speaker"
                R.id.rbListener -> userType = "listener"
                R.id.rbAssistant -> userType = "assistant"
                R.id.rbVisitor -> userType = "visitor"
            }
        }

        var res = sharedPreferences?.getString(DIMISSION, "1280x720");
        var rgResolution = findViewById<RadioGroup>(R.id.rgResolution)
        if ("1280x720".equals(res)) {
            rgResolution.check(R.id.rb720)
            resolution = "1280x720"
        } /*else if ("1920x1080".equals(res)) {
            rgResolution.check(R.id.rb1080)
            resolution = "1920x1080"
        }*/ else if ("968x762".equals(res)) {
            rgResolution.check(R.id.rb968)
            resolution = "968x762"
        }

        findViewById<Button>(R.id.btnEnterLiveClass).setOnClickListener {
            launchLiveClass()
        }

        findViewById<SwitchCompat>(R.id.blue_tooth_link).setOnCheckedChangeListener { btn, isChecked ->
            enableBlueTooth = isChecked
        }

        findViewById<SwitchCompat>(R.id.switchVideoStream).setOnCheckedChangeListener { buttonView, isChecked ->
            videoStream = if (isChecked) 18 else -1
        }

        findViewById<SwitchCompat>(R.id.ppt_interact).setOnCheckedChangeListener { btn, isChecked ->
            enablePptInteract = isChecked
        }

        findViewById<SwitchCompat>(R.id.undo_support_switch).setOnCheckedChangeListener { btn, isChecked ->
            undoSupport = isChecked
        }

        findViewById<SwitchCompat>(R.id.new_smallBoard).setOnCheckedChangeListener { btn, isChecked ->
            useNewSmallBoard = isChecked
        }

        findViewById<SwitchCompat>(R.id.selectSwitch).setOnCheckedChangeListener { btn, isChecked ->
            supportSelect = isChecked
        }
        var teach_tool_switch = findViewById<SwitchCompat>(R.id.teach_tool_switch)
        var layout_teach_tools = findViewById<LinearLayout>(R.id.layout_teach_tools)
        teach_tool_switch.setOnCheckedChangeListener { btn, isChecked ->
            layout_teach_tools.visibility = if (isChecked) VISIBLE else GONE
            teachToolTypes = if (isChecked) TeacherToolType.PureUpimeTeachToolTypeAll.value else 0
        }

        var teaching_method = findViewById<SwitchCompat>(R.id.teaching_method)
        var tv_auxiliary_cam = findViewById<TextView>(R.id.tv_auxiliary_cam)
        var axuiliary_cam = findViewById<SwitchCompat>(R.id.axuiliary_cam)
        teaching_method.setOnCheckedChangeListener { btn, isChecked ->
            isPhoneTeachingMethod = isChecked;
            tv_auxiliary_cam.visibility = if (isPhoneTeachingMethod) VISIBLE else GONE;
            axuiliary_cam.visibility = if (isPhoneTeachingMethod) VISIBLE else GONE;
        }

        var resident_camera = findViewById<SwitchCompat>(R.id.resident_camera)
        resident_camera.setOnCheckedChangeListener { btn, isChecked ->
            isResidentCamera = isChecked;
        }

        axuiliary_cam.setOnCheckedChangeListener { btn, isChecked ->
            isAuxiliaryCamera = isChecked;
        }

        findViewById<SwitchCompat>(R.id.sc_new_ppt).setOnCheckedChangeListener { btn, isChecked ->
            isNewPpt = isChecked;
            DemoApp.newPPt = isNewPpt
        }

        findViewById<SwitchCompat>(R.id.sc_log_level).setOnCheckedChangeListener { btn, isChecked ->
            findViewById<RadioGroup>(R.id.rg_log_level).visibility = if (isChecked) VISIBLE else GONE;
        }

        findViewById<SwitchCompat>(R.id.sc_new_quiz).setOnCheckedChangeListener { btn, isChecked ->
            supportNewQuiz = isChecked
        }

        findViewById<SwitchCompat>(R.id.sc_save_board).setOnCheckedChangeListener { btn, isChecked ->
            enableSaveBoard = isChecked
        }

        editElementMode = resources.getStringArray(R.array.edit_element_mode)[findViewById<Spinner>(R.id.sp_edit_element_mode).selectedItemPosition].toInt()
        findViewById<Spinner>(R.id.sp_edit_element_mode).onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    editElementMode = resources.getStringArray(R.array.edit_element_mode)[position].toInt()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where no item is selected if needed
                }
            }

        initCheckBox()
        initDefaultValue()
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_error ->
                    if (checked) {
                        logLevel = UpimeParameter.ERROR;
                    }
                R.id.radio_warn ->
                    if (checked) {
                        logLevel = UpimeParameter.WARN;
                    }
                R.id.radio_info ->
                    if (checked) {
                        logLevel = UpimeParameter.INFO;
                    }
                R.id.radio_debug ->
                    if (checked) {
                        logLevel = UpimeParameter.DEBUG;
                    }
            }
        }
    }

    private fun initDefaultValue() {
        etMeetingId.setText(sharedPreferences?.getString(MEETING_ID, ""))
        findViewById<EditText>(R.id.etName).setText(sharedPreferences?.getString(LOGIN_NAME, ""))
        findViewById<EditText>(R.id.etOnlineMode).setText(sharedPreferences?.getString(ONLINE_MODE, "1"))
    }

    private fun initCheckBox() {
        findViewById<CheckBox>(R.id.triangle_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.rect_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.ellipse_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.line_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.dashline_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.square_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.circle_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.arror_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.fanshaped_key).setOnCheckedChangeListener(this)
        findViewById<CheckBox>(R.id.parallelogram_key).setOnCheckedChangeListener(this)
    }

    private fun launchLiveClass() {
        var parameter: UpimeParameter = UpimeParameter()
        parameter.waterMark = "liveclassdemo"
        parameter.logLevel = logLevel
        parameter.appId = Config.appId
        val path: String = this.getExternalFilesDir("logs")!!.path
        parameter.logDir = path
        DemoApp.upime.setUpimeParameter(parameter)
        var hostaddr = findViewById<EditText>(R.id.hostaddr)
        var etColor= findViewById<EditText>(R.id.etColor)
        var remindTime= findViewById<EditText>(R.id.remindTime)
        var redPacketLimit= findViewById<EditText>(R.id.redPacketLimit)
        var etPermission= findViewById<EditText>(R.id.etPermission)

        val query = getQuery()
        if (query != null) {
            val config = ClassConfig().also {
                if (TextUtils.isEmpty(hostaddr.text)) {
                    it.classURL = query
                } else {
                    it.classURL = hostaddr.text.toString() + "?" + query
                }
                it.host = Config.server

                it.openFileMode = openFileMode
                it.toolboxItems = toolBoxItems

                val keyForColor = etColor.text.toString().trim()
                if (!TextUtils.isEmpty(keyForColor)) {
                    it.keyForColor = keyForColor
                }
                it.supportBlueToothConnect = enableBlueTooth
                it.enableInteractPpt = enablePptInteract
                it.teachToolTypes = teachToolTypes
                it.supportUndo = undoSupport
                it.useNewSmallBoard = useNewSmallBoard
                it.enableLiveSign = enableLiveSign
                it.enableVote = enableVote
                it.supportSelect = supportSelect;
                it.endRemindTime = remindTime.text.toString().toInt()
                var limitnum = 0
                if (!TextUtils.isEmpty(redPacketLimit.text?.toString())) {
                    limitnum = redPacketLimit.text.toString().toInt()
                }
                if (limitnum > 0) {
                    it.redPacketLimit = limitnum;
                }
                if (useMeetingMode && TextUtils.isDigitsOnly(etPermission.text.toString())) {
                    it.defaultPermission = etPermission.text.toString().toInt()
                }
                if (isPhoneTeachingMethod) {
                    it.mobileTeaching = true;
                }
                it.residentCamera = isResidentCamera;
                it.auxiliaryCamera = isAuxiliaryCamera;
                teachingList.add(
                    WebviewObject(
                        WebviewObject.TYPE_TEACH_MATERIAL,
                        "http://www.baidu.com",
                        "测试消息,打开百度"
                    )
                );
                teachingList.add(
                    WebviewObject(
                        2,
                        "http://www.bing.com",
                        "测试消息,打开bing"
                    )
                );
                it.webviewList = teachingList;
                it.supportHighlighter = DemoApp.sp.getBoolean("supportHighlighter", false)
                it.enableSaveBoard = enableSaveBoard
            }
            DemoApp.upime.launchLiveClass(config, object : ILiveClassListener {
                override fun onLiveClassReady(upimeBoard: UpimeBoard?) {
                    DemoApp.upimeBoard = upimeBoard
                }

                override fun onExited(exitCode: Int, mid: String) {
                }

                override fun onSkinChanged(skinId: Int) {
                }

                override fun onUpimeBoardSaved(
                    p0: Context?,
                    p1: UpimeBoardData?,
                    p2: IBoardCallback?
                ) {
                    p2?.onSaveSuccess()
                }

            })
        }
    }

    private fun getQuery(): String? {
        val meetingId = etMeetingId.text.toString()
        if (TextUtils.isEmpty(meetingId)) {
            etMeetingId.error = "meetingId is empty"
            return null
        }
        editor?.putString(MEETING_ID, meetingId)

        val userName = etName.text.toString()

        if (TextUtils.isEmpty(userName)) {
            etName.error = "loginName is empty"
            return null
        }
        editor?.putString(LOGIN_NAME, userName)

        var endTime = findViewById<EditText>(R.id.endTime)
        val _endTime = endTime.text.toString().toInt()
        if (_endTime > 0 && System.currentTimeMillis() / 1000 > _endTime) {
            //TODO提示
            endTime.error = "endTime needs to be greater than startTime"
            return null
        }

        val onlineMode = etOnlineMode.text.toString()

        if (TextUtils.isEmpty(onlineMode)) {
            etName.error = "onlineMode is empty"
            return null
        }
        editor?.putString(ONLINE_MODE, onlineMode)
        editor?.putString(MEETING_TYPE, meetingType)
        editor?.putString(ROLE, userType)

        val videoDimensionId = rgVideoDimension.checkedRadioButtonId
        var sharpness = "10"
        if (videoDimensionId == R.id.rb10) {
            sharpness = "10"
        } else if (videoDimensionId == R.id.rb20) {
            sharpness = "20"
        } else if (videoDimensionId == R.id.rb30) {
            sharpness = "30"
        }

        var rgResolution = findViewById<RadioGroup>(R.id.rgResolution)
        val resolutionId = rgResolution.checkedRadioButtonId
        /*if (resolutionId == R.id.rb360) {
            resolution = "480x360"
        } else*/ if (resolutionId == R.id.rb720) {
            resolution = "1280x720"
        } /*else if (resolutionId == R.id.rb1080) {
            resolution = "1920x1080"
        } */ else if (resolutionId == R.id.rb968) {
            resolution = "968x762"
        }
        editor?.putString(DIMISSION, resolution)

        val videoStream = if (meetingType == "video" && findViewById<SwitchCompat>(R.id.switchVideoStream).isChecked) 18 else -1
        editor?.putInt(VIDEO_STREAM, videoStream)

        val params = mutableMapOf<String, Any>().also {
            it["appId"] = Config.appId
            it["appType"] = "liveclassSDK"
            it["beginTime"] = System.currentTimeMillis() / 1000
            if (endTime.text.toString().toInt() > 0) {
                it["endTime"] = _endTime
            }
            it["mediaType"] = meetingType
            it["meetingId"] = meetingId
            it["meetingType"] = if (useMeetingMode) "meeting" else "public"
            it["loginName"] = userName
            it["userName"] = userName
            it["userType"] = userType
            it["validTime"] = 1080000
            it["onlineMode"] = Integer.parseInt(onlineMode)
            it["d_sharpness"] = sharpness
            it["d_dimension"] = resolution
            if (videoStream != -1) {
                it["videoStream"] = videoStream
            }
            it["vendorType"] = rtcType
            it["enableNewClassExam"] = if (supportNewQuiz)  1 else 0
            it["d_enableObjectEraser"] = editElementMode
        }
        var gson = Gson()
        var info: String = gson.toJson(params)
        editor?.putString("params", info)

        editor?.apply()
        return SignHelper.sign(params, Config.appKey)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            ids[0] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeTRIANGLE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeTRIANGLE.value.inv())!!;
                }
            }
            ids[1] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeRECT.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeRECT.value.inv())!!;
                }
            }
            ids[2] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeELLIPSE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeELLIPSE.value.inv())!!;
                }
            }
            ids[3] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeLINE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeLINE.value.inv())!!;
                }
            }
            ids[4] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE.value.inv())!!;
                }
            }
            ids[5] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeSQUARE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeSQUARE.value.inv())!!;
                }
            }

            ids[6] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeCIRCLE.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeCIRCLE.value.inv())!!;
                }
            }

            ids[7] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeARROR.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeARROR.value.inv())!!;
                }
            }

            ids[8] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeFANSHAPED.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeFANSHAPED.value.inv())!!;
                }
            }

            ids[9] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypePARALLELOGRAM.value)!!
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypePARALLELOGRAM.value.inv())!!;
                }
            }
        }
    }
}
