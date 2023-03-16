package cn.plaso.liveclasssdkdemo

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import cn.plaso.liveclasssdkdemo.liveclass.LiveClassLaunchActivity
import cn.plaso.liveclasssdkdemo.minilesson.MiniLessonBrowserActivity
import cn.plaso.upime.UpimeConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    var openFileMode = UpimeConfig.OPEN_FILE_MODE_IMAGE
    var toolBoxItems = UpimeConfig.ToolBoxItem.ALL.value;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openFileMode = DemoApp.sp.getInt("openFileMode", openFileMode)
        btnLiveClass.setOnClickListener {
            startActivity(Intent(this, LiveClassLaunchActivity::class.java).apply {
                putExtra("OpenFileMode", openFileMode)
                putExtra("ToolBoxItem", toolBoxItems)
            })
        }
        btnMiniLesson.setOnClickListener {
            startActivity(
                Intent(this, MiniLessonBrowserActivity::class.java).apply {
                    putExtra("OpenFileMode", openFileMode)
                }
            )
        }

        when (openFileMode) {
            UpimeConfig.OPEN_FILE_MODE_WINDOW -> rbWindow.isChecked = true
            else -> rbImage.isChecked = true
        }

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
        initCheckBox()
    }

    private fun initCheckBox() {
        class_test.setOnCheckedChangeListener(this)
        timer_count.setOnCheckedChangeListener(this)
        small_board.setOnCheckedChangeListener(this)
        dice.setOnCheckedChangeListener(this)
        red_packet.setOnCheckedChangeListener(this)
        responder.setOnCheckedChangeListener(this)
        browser.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.class_test -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.CLASS_TEST.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.CLASS_TEST.value.inv());
                }
            }
            R.id.timer_count -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.TIMER_COUNT.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.TIMER_COUNT.value.inv());
                }
            }
            R.id.small_board -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.SMALL_BOARD.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.SMALL_BOARD.value.inv());
                }
            }
            R.id.dice -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.DICE.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.DICE.value.inv());
                }
            }
            R.id.red_packet -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.RED_PACKET.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.RED_PACKET.value.inv());
                }
            }
            R.id.responder -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.RESPONDER.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.RESPONDER.value.inv());
                }
            }

            R.id.browser -> {
                toolBoxItems = if (isChecked) {
                    toolBoxItems?.or(UpimeConfig.ToolBoxItem.BROWSER.value)
                } else {
                    toolBoxItems?.and(UpimeConfig.ToolBoxItem.BROWSER.value.inv());
                }
            }
        }
    }

}