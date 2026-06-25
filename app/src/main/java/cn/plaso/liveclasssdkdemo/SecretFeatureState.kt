package cn.plaso.liveclasssdkdemo

/**
 * 隐藏功能解锁状态，仅保存在内存中，应用进程结束后重置。
 */
object SecretFeatureState {
    var prepareLessonRevealed = false
    var advancedSettingsRevealed = false
}
