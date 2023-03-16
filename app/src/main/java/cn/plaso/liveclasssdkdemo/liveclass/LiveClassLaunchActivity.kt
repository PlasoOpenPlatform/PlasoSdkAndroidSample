package cn.plaso.liveclasssdkdemo.liveclass

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.DemoApp
import cn.plaso.liveclasssdkdemo.R
import cn.plaso.liveclasssdkdemo.SignHelper
import cn.plaso.upime.*
import cn.plaso.upime.ClassConfig.*
import com.google.gson.Gson
import com.plaso.plasoliveclassandroidsdk.upimeActivity
import kotlinx.android.synthetic.main.activity_liveclass_launch.*

class LiveClassLaunchActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var meetingType: String = "video"
    private var videoStream: Int = -1;
    private var userType: String = "listener"
    private var isNewProtocol = true
    private var openFileMode = UpimeConfig.OPEN_FILE_MODE_WINDOW
    private var enableBlueTooth = false
    private var enablePptInteract = false
    private var undoSupport = false
    private var useNewSmallBoard =  false;
    private var useMeetingMode = false;
    private var supportSelect = true;
    private var teachToolTypes : Int = 0
    private var toolBoxItems: Int = 0
    private var rtcType: Int = 2
    private var sharedPreferences : SharedPreferences? = null
    private var editor : SharedPreferences.Editor? = null
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
    private var teachingList : MutableList<WebviewObject> = ArrayList(16)

    private var ids = arrayOf(
        R.id.triangle_key,
        R.id.rect_key,
        R.id.ellipse_key,
        R.id.line_key,
        R.id.dashline_key,
        R.id.square_key,
        R.id.circle_key
    )


    enum class TeacherToolType(val value: Int) {
        PureUpimeTeachToolTypeTRIANGLE(1 shl 0),
        PureUpimeTeachToolTypeRECT(1 shl 1),
        PureUpimeTeachToolTypeELLIPSE(1 shl 2),
        PureUpimeTeachToolTypeLINE(1 shl 3),
        PureUpimeTeachToolTypeDASHEDLINE(1 shl 4),
        PureUpimeTeachToolTypeSQUARE(1 shl 5),
        PureUpimeTeachToolTypeCIRCLE(1 shl 6),
        PureUpimeTeachToolTypeAll(127)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liveclass_launch)

        sharedPreferences = this.getSharedPreferences("default", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()

        openFileMode = intent.getIntExtra("OpenFileMode", UpimeConfig.OPEN_FILE_MODE_IMAGE)
        toolBoxItems = intent.getIntExtra("ToolBoxItem", 0)
        rgAppId.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbNewAppId -> isNewProtocol = true
                R.id.rbOldAppId -> isNewProtocol = false
            }
            onProtocolChange()
        }
        var meettype = sharedPreferences?.getString(MEETING_TYPE, "video")
        if ("video".equals(meettype)) {
            rgMeetingType.check(R.id.rbVideo)
            meetingType = "video"
            videoStream = sharedPreferences?.getInt(VIDEO_STREAM, -1) ?: -1
            if (videoStream == 18) {
                switchVideoStream.isChecked = true;
            }
        } else {
            rgMeetingType.check(R.id.rbAudio)
            meetingType = "audio"
        }

        rgMeetingType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbVideo ->{
                    meetingType = "video"
                    switchVideoStream.visibility = VISIBLE
                }
                R.id.rbAudio -> {
                    meetingType = "audio"
                    switchVideoStream.isChecked = false
                    switchVideoStream.visibility = GONE
                }
            }
        }

        var usertype = sharedPreferences?.getString(ROLE, "listener")
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

        btnEnterLiveClass.setOnClickListener {
            launchLiveClass()
        }

        blue_tooth_link.setOnCheckedChangeListener { btn, isChecked ->
            enableBlueTooth = isChecked
        }

        switchVideoStream.setOnCheckedChangeListener { buttonView, isChecked ->
            videoStream = if (isChecked)  18 else -1
        }

        ppt_interact.setOnCheckedChangeListener { btn, isChecked ->
            enablePptInteract = isChecked
        }

        undo_support_switch.setOnCheckedChangeListener { btn, isChecked ->
            undoSupport = isChecked
        }

        new_smallBoard.setOnCheckedChangeListener { btn, isChecked ->
            useNewSmallBoard = isChecked
        }

        selectSwitch.setOnCheckedChangeListener { btn, isChecked ->
            supportSelect = isChecked
        }

        meeting_mode.setOnCheckedChangeListener { btn, isChecked ->
            useMeetingMode = isChecked
            if (isChecked) {
                tvPermission.visibility = VISIBLE;
                etPermission.visibility = VISIBLE;
            } else {
                tvPermission.visibility = GONE;
                etPermission.visibility = GONE;
            }
        }

        teach_tool_switch.setOnCheckedChangeListener { btn, isChecked ->
            layout_teach_tools.visibility = if (isChecked) VISIBLE else GONE
            teachToolTypes = if (isChecked) TeacherToolType.PureUpimeTeachToolTypeAll.value else 0
        }

        teaching_method.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                isPhoneTeachingMethod = true;
            } else {
                isPhoneTeachingMethod = false;
            }
            tv_auxiliary_cam.visibility = if(isPhoneTeachingMethod) VISIBLE else GONE;
            axuiliary_cam.visibility = if(isPhoneTeachingMethod) VISIBLE else GONE;
        }

        resident_camera.setOnCheckedChangeListener{ btn, isChecked ->
            if (isChecked) {
                isResidentCamera = true;
            } else {
                isResidentCamera = false;
            }
        }

        axuiliary_cam.setOnCheckedChangeListener{ btn, isChecked ->
            if (isChecked) {
                isAuxiliaryCamera = true;
            } else {
                isAuxiliaryCamera = false;
            }
        }

        sc_new_ppt.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                isNewPpt = true;
            } else {
                isNewPpt = false;
            }
            DemoApp.newPPt = isNewPpt
        }

        sc_log_level.setOnCheckedChangeListener { btn, isChecked ->
            rg_log_level.visibility = if(isChecked) VISIBLE else GONE;
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
        etName.setText(sharedPreferences?.getString(LOGIN_NAME, ""))
        etOnlineMode.setText(sharedPreferences?.getString(ONLINE_MODE, "1"))
    }

    private fun initCheckBox() {
        triangle_key.setOnCheckedChangeListener(this)
        rect_key.setOnCheckedChangeListener(this)
        ellipse_key.setOnCheckedChangeListener(this)
        line_key.setOnCheckedChangeListener(this)
        dashline_key.setOnCheckedChangeListener(this)
        square_key.setOnCheckedChangeListener(this)
        circle_key.setOnCheckedChangeListener(this)
    }

    private fun launchLiveClass() {
        var parameter : UpimeParameter = UpimeParameter()
        parameter.waterMark = "liveclassdemo"
        parameter.logLevel = logLevel
        DemoApp.upime.setUpimeParameter(parameter)
        if (isNewProtocol) {
            val query = getQuery()

            if (query != null) {
                val config = ClassConfig().also {
                    if (TextUtils.isEmpty(hostaddr.text)) {
                        it.classURL = query
                    } else {
                        it.classURL = hostaddr.text.toString() + "?" + query
                    }
                    it.host = Config.server
                    it.features =
                        VIDEO_MARK or SMALL_BOARD or RED_PACKET or CAST or QIANGDAQI or TOUZI

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
                    it.userNPPT = isNewPpt
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
                }
                DemoApp.upime.launchLiveClass(config, object : ILiveClassListener {
                    override fun onLiveClassReady(upimeBoard: UpimeBoard?) {
                        DemoApp.upimeBoard = upimeBoard
                    }

                    override fun onExited(exitCode: Int, mid: String) {
                    }

                    override fun onSkinChanged(skinId: Int) {
                    }

                })
            }
        } else {
            var intent = Intent(this, upimeActivity::class.java)
            if (TextUtils.isEmpty(etMeetingId.text)) {
                Toast.makeText(this, "地址为空", Toast.LENGTH_SHORT).show()
                return
            }
            if ("listener".equals(userType)) {
                //intent.putExtra(upimeActivity.EXTRA_URL,"https://wwwr.plaso.cn/static/sdk/styleupime/5.00/?appId=sdk&appType=liveclassSDK&beginTime=1618556752&cloudDisk=false&live=1&mediaType=video&meetingId=test111113&meetingType=public&userName=hello&userType=listener&validTime=10800&signature=534C1D24E94AA000CCDB2C1EC3D2469F3826F949");
                intent.putExtra(upimeActivity.EXTRA_URL, etMeetingId.text.toString())
                //老协议4:3, 没有适配平板端，所以只支持false.
                intent.putExtra(upimeActivity.EXTRA_IS_PAD, false)
                applicationContext.startActivity(intent)
            } else {
                Toast.makeText(this, "不支持类型 " + userType, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onProtocolChange() {
        if (isNewProtocol) {
            rbSpeaker.visibility = VISIBLE
            rbAssistant.visibility = VISIBLE
        } else {
            rbSpeaker.visibility = GONE
            rbAssistant.visibility = GONE
            etMeetingId.setHint("请输入老协议地址:")
            etName.visibility = GONE
            etOnlineMode.visibility = GONE
            etColor.visibility = GONE
            rbListener.isChecked = true
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

        val videoStream = if (meetingType == "video" && switchVideoStream.isChecked) 18 else -1
        editor?.putInt(VIDEO_STREAM, videoStream)

        val params = mutableMapOf<String, Any>().also {
            it["appId"] = Config.appId
            it["appType"] = "liveclassSDK"
            it["beginTime"] = System.currentTimeMillis() / 1000
            if (endTime.text.toString().toInt() > 0) {
                it["endTime"] = endTime.text.toString().toInt()
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
            if (!TextUtils.isEmpty(vendorType.text) && TextUtils.isDigitsOnly(vendorType.text)) {
                rtcType = Integer.parseInt(vendorType.text.toString())
            }
            it["vendorType"] = rtcType
        }
        var gson = Gson()
        var info : String = gson.toJson(params)
        editor?.putString("params", info)

        editor?.apply()
        return SignHelper.sign(params, Config.appKey)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            ids[0] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeTRIANGLE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeTRIANGLE.value.inv());
                }
            }
            ids[1] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeRECT.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeRECT.value.inv());
                }
            }
            ids[2] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeELLIPSE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeELLIPSE.value.inv());
                }
            }
            ids[3] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeLINE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeLINE.value.inv());
                }
            }
            ids[4] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeDASHEDLINE.value.inv());
                }
            }
            ids[5] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeSQUARE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeSQUARE.value.inv());
                }
            }

            ids[6] -> {
                teachToolTypes = if (isChecked) {
                    teachToolTypes?.or(TeacherToolType.PureUpimeTeachToolTypeCIRCLE.value)
                } else {
                    teachToolTypes?.and(TeacherToolType.PureUpimeTeachToolTypeCIRCLE.value.inv());
                }
            }
        }
    }
}