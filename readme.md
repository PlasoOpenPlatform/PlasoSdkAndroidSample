## 简要说明
- 在 `Config.kt` 中填入 `appId`、`appKey` 后编译运行即可体验。
- `ossToken` 是微课上传时传入的参数，仅供参考；保存到伯索云 OSS 时不需要传入。

## 体验包下载（Release APK）

已提供签名好的体验 APK（当前对应 `styleupime` **1.68.110**），可直接安装体验，无需先编译工程。

- **推荐（OSS）**：[PlasoSdkAndroidSample-release.apk](http://hz-public-files.oss-cn-hangzhou.aliyuncs.com/dev-plaso/dev-prod/PlasoSdkAndroidSample-release.apk)
- GitHub Release（备用）：https://github.com/PlasoOpenPlatform/PlasoSdkAndroidSample/releases/latest

手机浏览器扫描下方二维码即可下载安装（OSS）：

![Demo APK 下载二维码](docs/demo-apk-qrcode.png)

> 安装后请在 App 内配置伯索分配的 `appId` / `appKey`。正式对接时密钥只能保存在业务服务端，不要把正式密钥写进生产 App。
