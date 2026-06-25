package cn.plaso.liveclasssdkdemo

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.forceLandscapeOnPhone() {
    if (resources.configuration.smallestScreenWidthDp < 600) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}
