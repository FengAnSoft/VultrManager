# Vultr Manager

一个使用 **Vultr API v2** 的第三方原生 Android 管理客户端，采用 Kotlin + Jetpack Compose 构建，可让你随时查看云服务器状态、创建/销毁实例、管理电源、查看账户账单与流量监控，并支持深色模式。

> 本应用为独立管理工具，与 Vultr 官方无隶属关系。API Key 仅通过 Android 密钥库加密保存在本地，不会硬编码或上传。

---

## 功能特性

### 实例管理
- **实例列表**：展示所有 Vultr 云服务器，显示名称、状态、IP、区域等关键信息。
- **搜索与筛选**：支持按名称 / IP / 区域搜索；可按运行中、已关机、部署中筛选状态。
- **排序**：支持按名称 A→Z / Z→A、状态、创建时间排序。
- **实例详情**：查看电源状态、公网/内网 IP、区域、套餐、操作系统、vCPU、内存、磁盘、月费用、月流量额度、创建时间、防火墙组、自动备份、SSH 密钥数等。
- **电源控制**：开机、关机、重启实例。
- **销毁实例**：危险操作二次确认后永久删除实例及数据。
- **创建实例**：通过表单选择区域、套餐、操作系统、启用 IPv6、SSH 密钥、自动备份、标签等，提交后自动刷新列表。

### 账户与监控
- **账户与账单**：查看账户余额、当月已计费和待计费金额。
- **流量监控**：展示实例本月入站/出站带宽使用情况（含图表）。

### 应用设置
- **API Key 配置**：首次启动需输入 Vultr API Key，保存时验证有效性。
- **安全存储**：使用 `EncryptedSharedPreferences`（Android 密钥库，AES-256-GCM）加密保存 API Key。
- **深色模式**：支持浅色/深色主题切换，即时生效。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM + ViewModelProvider.Factory |
| 导航 | Navigation Compose |
| 网络 | Retrofit2 + Gson + OkHttp |
| 协程 | Kotlinx Coroutines |
| 安全 | AndroidX Security Crypto（EncryptedSharedPreferences） |
| 最低版本 | Android 6.0（API 23） |
| 编译/目标版本 | API 34 |

---

## 根目录截图说明

项目中包含四张来自真机/模拟器的运行截图，文件名即拍摄时间，对应界面如下：

### 1. `S60810-23234541_com.example.vultrmanager.png` — 实例列表
- 顶部标题栏显示「Vultr 实例」，右侧为刷新、账户、设置入口。
- 搜索框支持按名称 / IP / 区域查找。
- 下方 `全部`、`运行中`、`已关机`、`部署中` 四个筛选标签。
- 右下角排序菜单按钮，支持多种排序方式。
- 实例卡片展示单台服务器信息（截图中已做模糊处理）。
- 右下角悬浮按钮（+）用于创建新实例。

### 2. `S60810-23241567_com.example.vultrmanager.png` — 实例详情（上半部分）
- **电源管理**：状态标签显示「运行中」，提供开机、关机、重启按钮。
- **流量监控（本月）**：展示入站/出站带宽，当前无数据时显示「暂无带宽数据」及图例。
- **基本信息**：名称、ID、状态、电源状态、标签、主机名等字段。
- **网络**：公网 IP、内网 IP、IPv6 是否启用。

### 3. `S60810-23250296_com.example.vultrmanager.png` — 实例详情（下半部分）与销毁
- **配置**：区域、套餐、操作系统、vCPU、内存、磁盘、月费用、月流量额度。
- **其他**：创建时间、防火墙组、自动备份、SSH 密钥数。
- **危险操作**：红色卡片提示销毁实例将永久删除数据，提供「销毁实例」按钮进行二次确认。

### 4. `S60810-23254881_com.example.vultrmanager.png` — API Key 设置
- 标题「API Key 设置」。
- 说明文字：API Key 会通过 Android 密钥库（AES-256-GCM）加密后保存在本机。
- **深色模式**开关：切换浅色/深色界面主题。
- **Vultr API Key** 输入框：支持显示/隐藏密码。
- **保存并验证**按钮：保存并校验 Key 是否有效。
- 底部提示「已保存有效的 API Key。」表示验证通过。

---

## 快速开始

### 1. 获取 Vultr API Key
- 登录 [Vultr 控制台](https://my.vultr.com/)。
- 进入 **Account → API → Add API Key**，生成一个具有所需权限的 API Key。
- 建议仅开启读取账户、实例、账单等必要权限。

### 2. 构建与运行
1. 使用 Android Studio（推荐 Jellyfish 或更新版本）打开本项目。
2. 等待 Gradle Sync 完成，下载依赖。
3. 连接设备或启动模拟器（Android 6.0+）。
4. 点击 **Run**（▶）构建并安装应用。

### 3. 首次使用
- 安装后首次启动进入 **API Key 设置** 页。
- 输入你的 Vultr API Key，点击「保存并验证」。
- 验证通过后自动跳转到实例列表，即可开始管理服务器。

---

## 项目结构

```
VultrManager/
├── app/src/main/java/com/example/vultrmanager/
│   ├── data/
│   │   ├── remote/              # Retrofit API 接口与数据模型
│   │   ├── local/               # EncryptedSharedPreferences、ThemeStore
│   │   └── VultrRepository.kt   # 统一数据仓库
│   ├── ui/
│   │   ├── MainActivity.kt      # 应用入口与导航图
│   │   ├── instances/           # 实例列表、详情、创建
│   │   ├── account/             # 账户与账单
│   │   ├── settings/            # API Key 与主题设置
│   │   ├── components/          # 通用组件（状态标签、带宽图表等）
│   │   └── theme/               # Compose 主题
│   └── VultrManagerApp.kt       # Application 与依赖容器
├── app/src/main/res/drawable/ic_launcher.xml  # 应用图标
└── README.md
```

---

## 注意事项

- 本应用仅调用 **Vultr API v2** 标准接口，未使用任何非官方接口。
- 销毁实例为不可逆操作，请谨慎操作。
- 由于 Vultr API 流量监控数据可能存在延迟，新建实例可能需要一段时间后才会显示带宽图表。
- 深色模式状态使用普通 `SharedPreferences` 保存；API Key 使用 `EncryptedSharedPreferences` 加密保存。

---

## 许可

本项目为学习/自用项目，未指定特定开源许可。
