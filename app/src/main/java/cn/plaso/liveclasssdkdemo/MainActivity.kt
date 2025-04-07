package cn.plaso.liveclasssdkdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import cn.plaso.liveclasssdkdemo.liveclass.LiveClassLaunchActivity
import cn.plaso.liveclasssdkdemo.minilesson.MiniLessonBrowserActivity
import cn.plaso.upime.UpimeConfig

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    var openFileMode = UpimeConfig.OPEN_FILE_MODE_IMAGE
    var toolBoxItems = UpimeConfig.ToolBoxItem.ALL.value;
    var supportHighlighter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openFileMode = DemoApp.sp.getInt("openFileMode", openFileMode)
        supportHighlighter = DemoApp.sp.getBoolean("supportHighlighter", false)
        var btnLiveClass = findViewById<Button>(R.id.btnLiveClass)
        val sign_in = findViewById<CheckBox>(R.id.sign_in)
        val vote = findViewById<CheckBox>(R.id.vote_up)
        btnLiveClass.setOnClickListener {
            startActivity(Intent(this, LiveClassLaunchActivity::class.java).apply {
                putExtra("OpenFileMode", openFileMode)
                putExtra("ToolBoxItem", toolBoxItems)
                putExtra("SupportSignIn", sign_in.isChecked);
                putExtra("SupportVote", vote.isChecked)
            })
        }
        var btnMiniLesson = findViewById<Button>(R.id.btnMiniLesson)
        btnMiniLesson.setOnClickListener {
            startActivity(
                Intent(this, MiniLessonBrowserActivity::class.java).apply {
                    putExtra("OpenFileMode", openFileMode)
                }
            )
        }
        var rbWindow = findViewById<RadioButton>(R.id.rbWindow)
        var rbImage = findViewById<RadioButton>(R.id.rbImage)
        when (openFileMode) {
            UpimeConfig.OPEN_FILE_MODE_WINDOW -> rbWindow.isChecked = true
            else -> rbImage.isChecked = true
        }
        var rgOpenFileMode = findViewById<RadioGroup>(R.id.rgOpenFileMode)
        rgOpenFileMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbImage -> {
                    openFileMode = UpimeConfig.OPEN_FILE_MODE_IMAGE
                }
                R.id.rbWindow -> {
                    openFileMode = UpimeConfig.OPEN_FILE_MODE_WINDOW
                }
            }

            DemoApp.sp.edit().putInt("openFileMode", openFileMode).apply()
        }
        var sw_highlighter = findViewById<SwitchCompat>(R.id.sw_highlighter)
        sw_highlighter.isChecked = supportHighlighter
        sw_highlighter.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            supportHighlighter = isChecked
            DemoApp.sp.edit().putBoolean("supportHighlighter", supportHighlighter).apply()
        })
        initCheckBox()
    }

    private fun initCheckBox() {
        var class_test = findViewById<CheckBox>(R.id.class_test)
        class_test.setOnCheckedChangeListener(this)
        var timer_count = findViewById<CheckBox>(R.id.timer_count)
        timer_count.setOnCheckedChangeListener(this)
        var small_board = findViewById<CheckBox>(R.id.small_board)
        small_board.setOnCheckedChangeListener(this)
        var dice = findViewById<CheckBox>(R.id.dice)
        dice.setOnCheckedChangeListener(this)
        var red_packet = findViewById<CheckBox>(R.id.red_packet)
        red_packet.setOnCheckedChangeListener(this)
        var responder = findViewById<CheckBox>(R.id.responder)
        responder.setOnCheckedChangeListener(this)
        var browser = findViewById<CheckBox>(R.id.browser)
        browser.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.class_test -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.CLASS_TEST.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.CLASS_TEST.value.inv())!!;
                }
            }
            R.id.timer_count -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.TIMER_COUNT.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.TIMER_COUNT.value.inv())!!;
                }
            }
            R.id.small_board -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.SMALL_BOARD.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.SMALL_BOARD.value.inv())!!;
                }
            }
            R.id.dice -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.DICE.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.DICE.value.inv())!!;
                }
            }
            R.id.red_packet -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.RED_PACKET.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.RED_PACKET.value.inv())!!;
                }
            }
            R.id.responder -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.RESPONDER.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.RESPONDER.value.inv())!!;
                }
            }

            R.id.browser -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.BROWSER.value)!!
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.BROWSER.value.inv())!!;
                }
            }
        }
    }

}
