package cn.plaso.liveclasssdkdemo.minilesson

import cn.plaso.liveclasssdkdemo.Config
import cn.plaso.liveclasssdkdemo.config.MiniLessonSettings
import cn.plaso.upime.OSSParams
import com.google.gson.Gson

object OssTokenHelper {
    fun getEffectiveToken(): String {
        val fromConfig = Config.OSS_TOKEN.trim()
        if (fromConfig.isNotEmpty()) {
            return fromConfig
        }
        return MiniLessonSettings.ossToken.trim()
    }

    fun isTokenEmpty(): Boolean = getEffectiveToken().isEmpty()

    fun parseOssParams(gson: Gson): OSSParams? {
        val token = getEffectiveToken()
        if (token.isEmpty()) {
            return null
        }
        return try {
            gson.fromJson(token, StsToken::class.java)?.toOssParams()
        } catch (_: Exception) {
            null
        }
    }

    fun isValidTokenJson(gson: Gson, token: String): Boolean {
        if (token.isBlank()) {
            return false
        }
        return try {
            val stsToken = gson.fromJson(token.trim(), StsToken::class.java) ?: return false
            !stsToken.accessKeyId.isNullOrBlank() &&
                !stsToken.accessKeySecret.isNullOrBlank() &&
                !stsToken.stsToken.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }
}
