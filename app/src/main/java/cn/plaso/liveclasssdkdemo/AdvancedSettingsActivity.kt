package cn.plaso.liveclasssdkdemo

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import cn.plaso.liveclasssdkdemo.config.AdvancedConfig
import cn.plaso.upime.UpimeParameter

class AdvancedSettingsActivity : AppCompatActivity() {

    private lateinit var etRtcType: EditText
    private lateinit var etHostAddr: EditText
    private lateinit var etEndRemindTime: EditText
    private lateinit var rgResolution: RadioGroup
    private lateinit var rgLogLevel: RadioGroup
    private lateinit var swUploadLog: SwitchCompat
    private lateinit var swMeetingMode: SwitchCompat
    private lateinit var layoutMeetingPermission: View
    private lateinit var etMeetingPermission: EditText
    private lateinit var swLiveStreaming: SwitchCompat
    private lateinit var swEnableRecallMsg: SwitchCompat
    private lateinit var swEnableLiveclassSignRollcall: SwitchCompat
    private lateinit var swMobileTeaching: SwitchCompat
    private lateinit var swAuxiliaryCamera: SwitchCompat
    private lateinit var swRearCamera: SwitchCompat
    private lateinit var swOfflineSign: SwitchCompat
    private lateinit var swShareBoard: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceLandscapeOnPhone()
        setContentView(R.layout.activity_advanced_settings)

        etRtcType = findViewById(R.id.etRtcType)
        etHostAddr = findViewById(R.id.etHostAddr)
        etEndRemindTime = findViewById(R.id.etEndRemindTime)
        rgResolution = findViewById(R.id.rgResolution)
        rgLogLevel = findViewById(R.id.rgLogLevel)
        swUploadLog = findViewById(R.id.swUploadLog)
        swMeetingMode = findViewById(R.id.swMeetingMode)
        layoutMeetingPermission = findViewById(R.id.layoutMeetingPermission)
        etMeetingPermission = findViewById(R.id.etMeetingPermission)
        swLiveStreaming = findViewById(R.id.swLiveStreaming)
        swEnableRecallMsg = findViewById(R.id.swEnableRecallMsg)
        swEnableLiveclassSignRollcall = findViewById(R.id.swEnableLiveclassSignRollcall)
        swMobileTeaching = findViewById(R.id.swMobileTeaching)
        swAuxiliaryCamera = findViewById(R.id.swAuxiliaryCamera)
        swRearCamera = findViewById(R.id.swRearCamera)
        swOfflineSign = findViewById(R.id.swOfflineSign)
        swShareBoard = findViewById(R.id.swShareBoard)

        loadSettings()

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveSettings()
            Toast.makeText(this, getString(R.string.config_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSettings() {
        etRtcType.setText(AdvancedConfig.rtcType.toString())
        etHostAddr.setText(AdvancedConfig.hostAddr)
        etEndRemindTime.setText(AdvancedConfig.endRemindTime.toString())

        when (AdvancedConfig.logLevel) {
            UpimeParameter.DEBUG -> rgLogLevel.check(R.id.rbLogDebug)
            UpimeParameter.INFO -> rgLogLevel.check(R.id.rbLogInfo)
            UpimeParameter.WARN -> rgLogLevel.check(R.id.rbLogWarn)
            UpimeParameter.ERROR -> rgLogLevel.check(R.id.rbLogError)
        }
        if (AdvancedConfig.resolution == "968x726") {
            rgResolution.check(R.id.rb968)
        } else {
            rgResolution.check(R.id.rb720)
        }

        swUploadLog.isChecked = AdvancedConfig.enableUploadLog
        swMeetingMode.isChecked = AdvancedConfig.useMeetingMode
        etMeetingPermission.setText(AdvancedConfig.meetingDefaultPermission.toString())
        layoutMeetingPermission.visibility = if (swMeetingMode.isChecked) View.VISIBLE else View.GONE
        swMeetingMode.setOnCheckedChangeListener { _, isChecked ->
            layoutMeetingPermission.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        swLiveStreaming.isChecked = AdvancedConfig.liveStreaming
        swEnableRecallMsg.isChecked = AdvancedConfig.enableRecallMsg
        swEnableLiveclassSignRollcall.isChecked = AdvancedConfig.enableLiveclassSignRollcall
        swMobileTeaching.isChecked = AdvancedConfig.mobileTeaching
        swAuxiliaryCamera.isChecked = AdvancedConfig.auxiliaryCamera
        swAuxiliaryCamera.visibility = if (swMobileTeaching.isChecked) View.VISIBLE else View.GONE
        swMobileTeaching.setOnCheckedChangeListener { _, isChecked ->
            swAuxiliaryCamera.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        swRearCamera.isChecked = AdvancedConfig.enableRearCamera
        swOfflineSign.isChecked = AdvancedConfig.enableLiveOfflineSign
        swShareBoard.isChecked = AdvancedConfig.canShareBoardToHistory
    }

    private fun saveSettings() {
        AdvancedConfig.rtcType = etRtcType.text.toString().toIntOrNull() ?: 2
        AdvancedConfig.hostAddr = etHostAddr.text.toString()
        AdvancedConfig.endRemindTime = etEndRemindTime.text.toString().toIntOrNull() ?: 0

        AdvancedConfig.logLevel = when (rgLogLevel.checkedRadioButtonId) {
            R.id.rbLogDebug -> UpimeParameter.DEBUG
            R.id.rbLogInfo -> UpimeParameter.INFO
            R.id.rbLogWarn -> UpimeParameter.WARN
            R.id.rbLogError -> UpimeParameter.ERROR
            else -> UpimeParameter.INFO
        }
        AdvancedConfig.resolution = if (rgResolution.checkedRadioButtonId == R.id.rb968) "968x726" else "1280x720"

        AdvancedConfig.enableUploadLog = swUploadLog.isChecked
        AdvancedConfig.useMeetingMode = swMeetingMode.isChecked
        val permissionValue = etMeetingPermission.text.toString().toIntOrNull() ?: 0
        AdvancedConfig.meetingDefaultPermission = if (permissionValue in 0..7) permissionValue else 0
        AdvancedConfig.liveStreaming = swLiveStreaming.isChecked
        AdvancedConfig.enableRecallMsg = swEnableRecallMsg.isChecked
        AdvancedConfig.enableLiveclassSignRollcall = swEnableLiveclassSignRollcall.isChecked
        AdvancedConfig.mobileTeaching = swMobileTeaching.isChecked
        AdvancedConfig.auxiliaryCamera = swAuxiliaryCamera.isChecked
        AdvancedConfig.enableRearCamera = swRearCamera.isChecked
        AdvancedConfig.enableLiveOfflineSign = swOfflineSign.isChecked
        AdvancedConfig.canShareBoardToHistory = swShareBoard.isChecked
    }
}
