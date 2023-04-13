package cn.plaso.liveclasssdkdemo

import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SignHelper {

    /**
     * Sign the [params] with specific [signKey]
     */
    fun sign(params: MutableMap<String, Any>, signKey: String): String {
        val keys = params.keys.sorted()
        val sortedParams = StringBuilder()
        for ((index, key) in keys.withIndex()) {
            if (index != 0) {
                sortedParams.append("&")
            }
            sortedParams.append("$key=${params[key]}")
        }

        val signature = encrypt(sortedParams.toString(), signKey)
        params["signature"] = signature;
        return buildQuery(params);
    }

    private fun buildQuery(params: MutableMap<String, Any>): String {
        val result = StringBuilder()
        for ((index, key) in params.keys.withIndex()) {
            if (index != 0) {
                result.append("&")
            }
            result.append("$key=${URLEncoder.encode(params[key].toString(), "UTF-8")}")
        }
        return result.toString()
    }

    private fun encrypt(encryptText: String, signKey: String): String {
        val algorithm = "HmacSHA1"
        val charset = Charset.forName("UTF-8")
        val data = signKey.toByteArray(charset)

        val secretKey = SecretKeySpec(data, algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKey)
        val rst = mac.doFinal(encryptText.toByteArray(charset))

        return BigInteger(1, rst).toString(16).toUpperCase()

    }

}