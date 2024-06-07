package cn.plaso.liveclasssdkdemo.minilesson

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
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
import cn.plaso.liveclasssdkdemo.liveclass.LiveClassLaunchActivity
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
    private var isNewPpt : Boolean = false
    private var openFileMode = UpimeConfig.OPEN_FILE_MODE_WINDOW
    private var toolBoxItems = UpimeConfig.ToolBoxItem.ALL.value
    private var recordType = MiniLessonConfig.RECORD_TYPE_AUDIO
    // private var openFileMode = UpimeConfig.OPEN_FILE_MODE_IMAGE

    private lateinit var btnCreateMiniLesson: Button
    private lateinit var rvMiniLessonList: RecyclerView
    private lateinit var teach_tool_switch: SwitchCompat

    private lateinit var undo_switch: SwitchCompat
    private lateinit var save_draft_switch: SwitchCompat
    private lateinit var interact_ppt: SwitchCompat
    private lateinit var recordtype: RadioGroup
    private lateinit var sc_new_ppt: SwitchCompat
    private lateinit var config_key: EditText
    private lateinit var mini_title: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_lesson_browser)
        openFileMode = intent.getIntExtra("OpenFileMode", UpimeConfig.OPEN_FILE_MODE_IMAGE)
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

       teach_tool_switch = findViewById(R.id.teach_tool_switch)
       teach_tool_switch.setOnCheckedChangeListener { btn, isChecked ->
           teachToolTypes = if (isChecked) LiveClassLaunchActivity.TeacherToolType.PureUpimeTeachToolTypeAll.value else 0
       }

       undo_switch = findViewById(R.id.undo_switch)
       undo_switch.setOnCheckedChangeListener { btn, isChecked ->
           supportUndo = isChecked
       }

        save_draft_switch = findViewById(R.id.save_draft_switch)
       save_draft_switch.setOnCheckedChangeListener { btn, isChecked ->
           supportSaveDraft = isChecked
       }

        interact_ppt = findViewById(R.id.interact_ppt)
        interact_ppt.setOnCheckedChangeListener { btn, isChecked ->
            enablePptInteract = isChecked
        }

        recordtype = findViewById(R.id.recordtype)
        recordtype.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAudioType -> {
                    recordType = MiniLessonConfig.RECORD_TYPE_AUDIO
                }
                R.id.rbVideoType -> {
                    recordType = MiniLessonConfig.RECORD_TYPE_VIDEO
                }
            }
        }
        sc_new_ppt = findViewById(R.id.sc_new_ppt)
        sc_new_ppt.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked) {
                isNewPpt = true;
            } else {
                isNewPpt = false;
            }
        }

        config_key = findViewById(R.id.config_key)
        config_key.setText(miniLessonViewModel.configKey)

        config_key.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val configKey = config_key.text.toString()
                if (!TextUtils.isEmpty(configKey)) {
                    miniLessonViewModel.configKey = configKey
                }
            }

        });
        mini_title = findViewById(R.id.mini_title)

   }

    override fun onResume() {
        super.onResume()
        miniLessonViewModel.getMiniLessonList()
    }

    private fun startMiniLesson(lessonInfoWrap: LessonInfoWrap?) {
        val config = MiniLessonConfig()
       // config.openFileMode = openFileMode
        if (lessonInfoWrap == null) {
            if (!TextUtils.isEmpty(mini_title.text.toString())) {
                config.topic = mini_title.text.toString()      //"微课测试_" + Random(System.currentTimeMillis()).nextInt()
            } else {
                //config.topic = "微课测试_" + Random(System.currentTimeMillis()).nextInt()
            }
            config.draftPath =
                    "${getExternalFilesDir(null)?.absolutePath}/mini/${System.currentTimeMillis()}"
        } else {
            config.topic = lessonInfoWrap.lessonInfo.topic
            config.draftPath = lessonInfoWrap.lessonInfo.path
        }

        File(config.draftPath).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }

        config.supportDraft = supportSaveDraft;
        config.supportUndo = supportUndo;
        config.teachToolTypes = teachToolTypes
        config.openFileMode = openFileMode
        config.recordType = recordType
        config.enableInteractPpt = enablePptInteract

        config.keyForColor = miniLessonViewModel.configKey
        config.pptType = if (DemoApp.newPPt) UpimeObject.TYPE_DX_PPT  else UpimeObject.TYPE_PPT
        config.fobiddenRecordScreen = true;
        config.supportHighlighter = DemoApp.sp.getBoolean("supportHighlighter", false)

        var extraJson = JSONObject()
        extraJson.put("id", "weikebao_id1");
        config.extra = extraJson.toString();

        DemoApp.upime.launchMiniLesson(config, object : IMiniLessonListener {
            override fun onMiniLessonReady(upimeBoard: UpimeBoard?) {
                DemoApp.upimeBoard = upimeBoard
            }

            override fun onClosed() = Unit

            override fun onDraftSaved(draft: LessonInfo?) {

                draft?.let {
                    Log.d(TAG, "onDraftSaved: ${it}\ndraftPath${config.draftPath}")
                }
            }

            override fun onFinished(info: LessonInfo?) {
                info?.let {
                    Log.d(TAG, "onFinished: ${it}\ndraftPath: ${config.draftPath}")
                }
                DemoApp.upimeBoard?.exit("close")
            }

            override fun onSkinChanged(skinId: Int) = Unit

        })
    }

    private fun upload(lessonInfoWrap: LessonInfoWrap) {

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
            it.ossParams =
                    GSON.fromJson(Config.ossToken, StsToken::class.java).toOssParams()

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
//        startMiniLesson(it)

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


}
