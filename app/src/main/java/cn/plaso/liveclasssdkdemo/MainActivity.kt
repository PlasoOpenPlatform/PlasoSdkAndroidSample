package cn.plaso.liveclasssdkdemo

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.plaso.liveclasssdkdemo.liveclass.LiveClassLaunchActivity
import cn.plaso.liveclasssdkdemo.preparelesson.PrepareLessonActivity
import cn.plaso.liveclasssdkdemo.minilesson.MiniLessonBrowserActivity

class MainActivity : AppCompatActivity() {

    private val prepareLessonReveal = SecretTapReveal()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initModuleButtons()
    }

    private fun initModuleButtons() {
        findViewById<Button>(R.id.btnLiveClass).setOnClickListener {
            startActivity(Intent(this, LiveClassLaunchActivity::class.java))
        }

        findViewById<Button>(R.id.btnMiniLesson).setOnClickListener {
            startActivity(Intent(this, MiniLessonBrowserActivity::class.java))
        }

        val btnPrepare = findViewById<Button>(R.id.btnPrepareLesson)
        btnPrepare.visibility = if (SecretFeatureState.prepareLessonRevealed) VISIBLE else GONE
        btnPrepare.setOnClickListener {
            startActivity(Intent(this, PrepareLessonActivity::class.java))
        }

        findViewById<TextView>(R.id.tvMainTitle).setOnClickListener {
            prepareLessonReveal.onTap {
                SecretFeatureState.prepareLessonRevealed = true
                btnPrepare.visibility = VISIBLE
            }
        }
    }
}
