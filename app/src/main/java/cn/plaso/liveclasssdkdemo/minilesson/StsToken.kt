package cn.plaso.liveclasssdkdemo.minilesson

import cn.plaso.upime.OSSParams

class StsToken(
    var expire: Long?,
    var accessKeyId: String?,
    var accessKeySecret: String?,
    var stsToken: String?,
    var uploadPath: String?,
    var provider: String?,
    var bucket: String?,
    var protocol: String?,
    var region: String?,
    var domain: String?
) {
    fun toOssParams(): OSSParams {
        val vender = when (provider) {
            "KSYUN" -> OSSParams.VENDOR_KSYUN
            "TENCENT" -> OSSParams.VENDOR_TENCENT
            "OBS" -> OSSParams.VENDOR_HUAWEI
            else -> OSSParams.VENDOR_ALI
        }
        return OSSParams(accessKeyId, accessKeySecret, stsToken, region, bucket, uploadPath, vender)
    }
}