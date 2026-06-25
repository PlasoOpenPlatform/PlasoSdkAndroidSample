package cn.plaso.liveclasssdkdemo

/**
 * 短时间内连续点击达到指定次数后触发回调，用于隐藏内部功能入口。
 */
class SecretTapReveal(
    private val requiredTaps: Int = 10,
    private val resetIntervalMs: Long = 2000L,
) {
    private var tapCount = 0
    private var lastTapTime = 0L

    fun onTap(onRevealed: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > resetIntervalMs) {
            tapCount = 0
        }
        lastTapTime = now
        tapCount++
        if (tapCount >= requiredTaps) {
            tapCount = 0
            onRevealed()
        }
    }
}
