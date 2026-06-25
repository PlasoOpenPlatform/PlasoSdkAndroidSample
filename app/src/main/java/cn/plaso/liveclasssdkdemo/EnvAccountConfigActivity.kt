package cn.plaso.liveclasssdkdemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import cn.plaso.Globals

class EnvAccountConfigActivity : AppCompatActivity() {

    private lateinit var etAppId: EditText
    private lateinit var etKey: EditText
    private lateinit var spEnv: Spinner
    private lateinit var spAppId: Spinner
    private lateinit var tvVersion: TextView
    private lateinit var layoutEnv: View
    private lateinit var layoutQuickSelect: View

    private lateinit var envArray: Array<String>
    private lateinit var appIdArray: List<String>
    private lateinit var appKeyArray: List<String>

    private val isExternalRelease: Boolean
        get() = resources.getStringArray(R.array.appId).isEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_env_account_config)

        etAppId = findViewById(R.id.etAppId)
        etKey = findViewById(R.id.etKey)
        spEnv = findViewById(R.id.spEnv)
        spAppId = findViewById(R.id.sp_appId)
        tvVersion = findViewById(R.id.version)
        layoutEnv = findViewById(R.id.layoutEnv)
        layoutQuickSelect = findViewById(R.id.layoutQuickSelect)

        tvVersion.text = getString(R.string.build_time_format, BuildConfig.debugVersion)

        if (isExternalRelease) {
            setupExternalMode()
        } else {
            setupInternalMode()
        }

        findViewById<Button>(R.id.btnFinish).setOnClickListener { confirmAndEnter() }
    }

    private fun setupExternalMode() {
        layoutEnv.visibility = View.GONE
        layoutQuickSelect.visibility = View.GONE
        applyEnv("www")
        etAppId.setText(DemoApp.sp.getString("appId", ""))
        etKey.setText(DemoApp.sp.getString("appKey", ""))
    }

    private fun setupInternalMode() {
        envArray = resources.getStringArray(R.array.env)
        val savedEnv = DemoApp.sp.getString("env", envArray[0]) ?: envArray[0]
        val envIndex = envArray.indexOf(savedEnv).coerceAtLeast(0)
        spEnv.setSelection(envIndex)
        applyEnv(envArray[envIndex])
        updateAppIdList(envArray[envIndex] == "dev")

        spEnv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val env = envArray[position]
                applyEnv(env)
                updateAppIdList(env == "dev")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun confirmAndEnter() {
        val appId = etAppId.text.toString().trim()
        val appKey = etKey.text.toString().trim()

        if (TextUtils.isEmpty(appId)) {
            etAppId.error = getString(R.string.internal_config_enter_app_id)
            return
        }
        if (TextUtils.isEmpty(appKey)) {
            etKey.error = getString(R.string.internal_config_enter_app_key)
            return
        }

        Config.appId = appId
        Config.appKey = appKey
        DemoApp.sp.edit {
            putString("appId", appId)
            putString("appKey", appKey)
        }

        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun applyEnv(env: String) {
        Config.server = "https://$env.plaso.cn"
        Globals.host = Config.server
        DemoApp.sp.edit { putString("env", env) }
    }

    private fun updateAppIdList(isDev: Boolean) {
        appIdArray = resources.getStringArray(if (isDev) R.array.appId else R.array.appId_for_test).toList()
        appKeyArray = resources.getStringArray(if (isDev) R.array.appKey else R.array.appKey_for_test).toList()
        if (appIdArray.isEmpty()) {
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, appIdArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAppId.adapter = adapter

        val currentAppId = DemoApp.sp.getString("appId", appIdArray[0])
        spAppId.setSelection(appIdArray.indexOf(currentAppId).coerceAtLeast(0))

        spAppId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etAppId.setText(appIdArray[position])
                etKey.setText(appKeyArray[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
