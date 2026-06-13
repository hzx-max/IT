# IT 运维学习平台（NetConfig）

> 面向 IT 运维工程师的综合知识管理与学习系统，涵盖网络命令、故障排查、桌面运维、Linux、Office 办公、AI 运维六大模块。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen.svg)](https://vuejs.org/)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange.svg)](https://adoptium.net/)
[![SQLite](https://img.shields.io/badge/SQLite-3.45-blue.svg)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/license-Private-lightgrey.svg)](#)

---

## 目录

- [快速开始](#快速开始)
- [系统架构](#系统架构)
- [模块总览](#模块总览)
- [前端架构](#前端架构)
- [后端架构](#后端架构)
- [API 文档](#api-文档)
- [数据库](#数据库)
- [权限体系](#权限体系)
- [操作指南](#操作指南)
- [技术细节](#技术细节)
- [开发指南](#开发指南)
- [部署](#部署)
- [项目统计](#项目统计)

---

## 快速开始

### 环境准备

| 工具 | 最低版本 | 验证命令 |
|------|----------|----------|
| JDK | 17 | `java -version` |
| Maven | 3.6 | `mvn -version` |
| Node.js | 18 | `node -v` |
| npm | 9 | `npm -v` |

### 启动后端

```powershell
cd springboot-backend
mvn spring-boot:run
```

首次启动会自动创建 `date.db` 及所有表，并初始化默认管理员账号。

后端运行在 `http://localhost:8080`

### 启动前端（新开终端）

```powershell
cd vue-frontend
npm install       # 首次运行需要安装依赖
npm run dev
```

前端运行在 `http://localhost:3000`

### 静默启动（推荐）

```powershell
# 后端（端口 8080，后台静默运行）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location -LiteralPath 'D:\桌面\IT运维学习平台\springboot-backend'; .\mvnw spring-boot:run" -WindowStyle Hidden

# 前端（端口 3000，后台静默运行）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location -LiteralPath 'D:\桌面\IT运维学习平台\vue-frontend'; npm run dev" -WindowStyle Hidden
```

### 端口说明

| 端口 | 服务 | 配置位置 |
|------|------|----------|
| 3000 | Vue 前端（Vite Dev Server） | `vue-frontend/vite.config.js` |
| 8080 | Spring Boot 后端（Tomcat） | `springboot-backend/src/main/resources/application.properties` |

---

## 系统架构

```
用户浏览器 (localhost:3000)
       │
       │ HTTP / Axios
       ▼
Vue 3 SPA（Vite Dev Server / Nginx）
  ├── 路由 (vue-router, 31 条路由)
  ├── 13 个共享组件
  ├── 30 个页面组件（6 模块 × 5 页面）
  ├── Tailwind CSS + 自定义全局样式
  └── html2pdf.js（PDF 导出）
       │
       │ /api/* (Axios, baseURL: /api)
       ▼
Spring Boot 3.2.5（Tomcat :8080）
  ├── Filter: RateLimitingFilter（登录限流）
  ├── Interceptor: AuthInterceptor（自定义 Token 鉴权 + CSRF 校验）
  ├── Controllers (14 个) → Services (11 个) → Repositories (17 个)
  │   ├── 业务模块: Command / Fault / Desktop / Linux / Office / AI
  │   ├── 通用模块: Auth / Profile / Upload / Click / Category / SearchHistory
  │   └── 审核流程: PendingChange
  ├── Config: WebConfig / CorsConfig / SecurityConfig / SpaForwardController
  └── DatabaseInitializer（启动时自动建表 + 初始化数据）
       │
       │ JDBC (SQLite)
       ▼
SQLite: date.db（嵌入式，零配置，单文件）
```

**详细请求流程：**
1. 浏览器访问 `localhost:3000` → Vite Dev Server 提供 Vue SPA
2. Vue 通过 Axios 代理（`/api` → `localhost:8080`）发送 HTTP 请求
3. 后端 `RateLimitingFilter` 对登录接口进行 IP 级别限流
4. `AuthInterceptor` 拦截所有 `/api/**` 请求，校验 Token 和权限
5. Controller 接收请求 → Service 处理业务逻辑 → Repository 存取数据库
6. 响应返回 JSON → Vue 渲染页面

---

## 模块总览

### 1. 网络命令（Command）— `/cmd/*`

**定位：** 网络设备配置命令库，按厂商分类的多配置版本管理。

**数据字段：** `id, title, cat, desc, detail, topo, configs: [{vendor, config, comment, doc, verificationCmd, verificationImages}], files, createdAt`

- **多厂商配置：** 一个知识点可包含华为、H3C、思科、锐捷、烽火、迈普、中兴 7 个厂商的配置命令
- **配置版本对比：** 每个厂商有独立配置文本、说明、验证命令、参考文档
- **拓扑图：** 关联网络拓扑图片/视频

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/cmd` | `CmdList.vue` | 卡片列表：搜索、分类筛选、拓扑图轮播、厂商标签 |
| `/cmd/detail/:id` | `CmdDetail.vue` | 详情：描述、拓扑图、厂商配置选项卡、PDF 导出、评论区 |
| `/cmd/admin` | `CmdAdmin.vue` | 管理表：复选框批量删除、行操作下拉（查看/编辑/删除） |
| `/cmd/add` | `CmdAdd.vue` | 添加表单：标题、分类、拓扑图、多厂商配置块、附件 |
| `/cmd/edit/:id` | `CmdEdit.vue` | 编辑表单（预填充，审核流程） |

### 2. 网络故障（Fault）— `/fault/*`

**定位：** 常见网络故障及排查方法的案例库。

**数据字段：** `id, title, category, images, videos, symptom, cause, solution, files, createdAt`

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/fault` | `FaultList.vue` | 卡片列表：图片/视频轮播、症状摘要 |
| `/fault/detail/:id` | `FaultDetail.vue` | 详情：症状、原因、解决方案、附件下载、评论区 |
| `/fault/admin` | `FaultAdmin.vue` | 管理表 |
| `/fault/add` | `FaultAdd.vue` | 添加表单 |
| `/fault/edit/:id` | `FaultEdit.vue` | 编辑表单 |

### 3. 桌面运维（Desktop）— `/desktop/*`

**定位：** 桌面终端常见问题与解决方案库。

**数据字段：** `id, title, category, images, videos, symptom, solution, files, createdAt`

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/desktop` | `DesktopList.vue` | 卡片列表 |
| `/desktop/detail/:id` | `DesktopDetail.vue` | 详情：症状、解决方案、附件 |
| `/desktop/admin` | `DesktopAdmin.vue` | 管理表 |
| `/desktop/add` | `DesktopAdd.vue` | 添加表单 |
| `/desktop/edit/:id` | `DesktopEdit.vue` | 编辑表单 |

### 4. Linux — `/linux/*`

**定位：** Linux 系统管理与配置指南。

**数据字段：** `id, title, vendor, cat, images, videos, desc, detail, config, verificationCmd, files, createdAt`

- **发行版分类：** CentOS、Ubuntu、Debian、RedHat、SUSE、Rocky、Alpine、Arch 等
- **分类标签：** 基础、文件、用户、网络、服务、磁盘、包管理、进程、防火墙、Shell、定时任务、备份、监控、安全

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/linux` | `LinuxList.vue` | 卡片列表：发行版 + 分类双标签 |
| `/linux/detail/:id` | `LinuxDetail.vue` | 详情：配置命令、验证命令 |
| `/linux/admin` | `LinuxAdmin.vue` | 管理表（发行版 + 分类列） |
| `/linux/add` | `LinuxAdd.vue` | 添加表单 |
| `/linux/edit/:id` | `LinuxEdit.vue` | 编辑表单 |

### 5. Office 办公 — `/office/*`

**定位：** Office 办公软件使用技巧与配置指南。

**数据字段：** `id, title, vendor, cat, images, videos, desc, detail, config, configComment, doc, verificationCmd, files, createdAt`

- **办公软件：** Word、Excel、PPT、Outlook、WPS（Word/Excel/PPT）、LibreOffice
- **分类标签：** 基础、格式、公式、图表、数据、邮件、宏、模板、打印、共享、安全、快捷键、样式、插入

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/office` | `OfficeList.vue` | 卡片列表 |
| `/office/detail/:id` | `OfficeDetail.vue` | 详情：配置内容、注释、文档 |
| `/office/admin` | `OfficeAdmin.vue` | 管理表 |
| `/office/add` | `OfficeAdd.vue` | 添加表单 |
| `/office/edit/:id` | `OfficeEdit.vue` | 编辑表单 |

### 6. AI 运维 — `/ai/*`

**定位：** AI 运维相关知识与提示词工程指南。

**数据字段：** `id, title, category, scenario, images, videos, prompt, config, desc, detail, files, createdAt`

- **特色字段：** `scenario`（场景描述）、`prompt`（提示词，代码块展示）、`config`（配置内容）

**页面列表：**
| 路由 | 页面 | 描述 |
|------|------|------|
| `/ai` | `AiList.vue` | 卡片列表：场景预览 |
| `/ai/detail/:id` | `AiDetail.vue` | 详情：提示词代码块、配置代码块 |
| `/ai/admin` | `AiAdmin.vue` | 管理表 |
| `/ai/add` | `AiAdd.vue` | 添加表单 |
| `/ai/edit/:id` | `AiEdit.vue` | 编辑表单 |

---

## 前端架构

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 渐进式 UI 框架（Composition API + `<script setup>`） |
| Vue Router | 4.3+ | 单页面路由（`createWebHistory` 模式） |
| Vite | 5.4+ | 构建工具 + 开发服务器 |
| Tailwind CSS | 3.4+ | 原子化样式框架 |
| Axios | 1.7+ | HTTP 请求库 |
| html2pdf.js | 0.10.1 | PDF 导出（动态 CDN 加载） |

### 目录结构

```
vue-frontend/src/
├── api/
│   ├── index.js         # 统一导出
│   ├── http.js          # Axios 实例（拦截器、Token 注入）
│   ├── modules.js       # 全部 API 端点（13 个模块）
│   ├── constants.js     # 厂商/分类枚举 + 颜色映射 + 日期格式化
│   └── approval.js      # 审核工作流辅助函数
├── assets/
│   └── main.css         # 全局样式（710 行：变量、按钮系统、布局、动画）
├── components/          # 共享组件（13 个）
├── composables/         # 组合式函数
│   └── useSidebarCollapse.js   # 侧边栏折叠/位置状态
├── layouts/
│   └── MainLayout.vue   # 应用外壳（侧边栏 + 内容区 + 响应式）
├── pages/               # 页面组件（30 个）
│   ├── HomePage.vue     # 首页仪表盘
│   ├── auth/            # 认证模块（5 个文件）
│   │   ├── AuthPage.vue       # 登录/注册滑动面板
│   │   ├── LoginPage.vue      # 登录页包装
│   │   ├── RegisterPage.vue   # 注册页包装
│   │   ├── ProfilePage.vue    # 个人中心（头像、资料编辑、历史/收藏）
│   │   └── SuperAdminPage.vue # 超级管理员面板（用户管理、审核变更）
│   ├── cmd/             # 网络命令模块（5 个页面）
│   ├── fault/           # 故障排查模块（5 个页面）
│   ├── desktop/         # 桌面运维模块（5 个页面）
│   ├── linux/           # Linux 模块（5 个页面）
│   ├── office/          # Office 模块（5 个页面）
│   └── ai/              # AI 运维模块（5 个页面）
├── router/
│   └── index.js         # 路由配置（31 条路由 + 导航守卫）
├── stores/
│   └── auth.js          # 权限状态管理（Token、用户名、角色）
├── utils/
│   ├── pdfExport.js     # PDF 导出引擎（html2canvas + html2pdf）
│   └── userLibrary.js   # 收藏（API 持久化）/ 浏览历史（localStorage）
├── App.vue              # 根组件（Token 校验 + 全局快捷键）
└── main.js              # 入口
```

### 共享组件一览

| 组件 | 文件 | 行数 | 用途 |
|------|------|------|------|
| Sidebar | `Sidebar.vue` | 290 | 可折叠/拖拽侧边栏导航，含 6 个模块分组菜单 |
| NavDropdown | `NavDropdown.vue` | 63 | 侧边栏菜单折叠子项 |
| SearchBar | `SearchBar.vue` | 160 | 全局搜索栏：关键词 + 供应商/分类下拉 + 搜索历史 |
| CategoryStrip | `CategoryStrip.vue` | 14 | 横向分类筛选胶囊按钮 |
| CardCarousel | `CardCarousel.vue` | 138 | 图片/视频轮播（自动播放 + 悬停放大） |
| DropdownSelect | `DropdownSelect.vue` | 109 | 通用下拉选择器（支持紧凑模式） |
| ComboBox | `ComboBox.vue` | 196 | 可搜索下拉框 + 分类管理（增删改） |
| ModalDialog | `ModalDialog.vue` | 55 | 确认/输入弹窗 |
| ToastMessage | `ToastMessage.vue` | 25 | 自动消失的通知提示 |
| FileUploader | `FileUploader.vue` | 287 | 拖拽文件上传（50+ 扩展名，10 种类型图标） |
| RelatedPanel | `RelatedPanel.vue` | 300 | 同类别内容浮动面板（可拖拽切换左/右/收起） |
| LearningNotes | `LearningNotes.vue` | 385 | 评论系统（点赞/回复/排序/审核） |
| FavoriteButton | `FavoriteButton.vue` | 44 | 收藏按钮（后端 API 持久化，游客回退 localStorage） |

### 路由结构

31 条路由，按模块分组：

| 路由模式 | 页面数 | 说明 |
|----------|--------|------|
| `/` | 1 | 首页仪表盘 |
| `/login`, `/register` | 2 | 登录/注册（共用 AuthPage） |
| `/super-admin` | 1 | 超级管理员面板 |
| `/profile` | 1 | 个人中心 |
| `/{module}` | 1 | 模块列表页 |
| `/{module}/detail/:id` | 1 | 详情页 |
| `/{module}/admin` | 1 | 管理页（需认证） |
| `/{module}/add` | 1 | 添加页（需认证） |
| `/{module}/edit/:id` | 1 | 编辑页（需认证） |

**导航守卫：** `router.beforeEach` 检查 `meta.requiresAuth`，未登录重定向到 `/login`；`requiresSuperAdmin` 额外检查 `SUPER_ADMIN` 角色；已登录用户访问登录页自动跳转至首页。

---

## 后端架构

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | Web 框架 |
| Spring Data JPA | 3.2.5 | ORM / 数据访问 |
| Hibernate | 6.x | JPA 实现 |
| SQLite JDBC | 3.45.1.0 | 数据库驱动 |
| Hibernate Community Dialects | 6.x | SQLite 方言支持 |
| Lombok | 1.18+ | `@Data`、`@RequiredArgsConstructor` 等代码生成 |
| Jackson | 2.x | JSON 序列化/反序列化 |
| Spring Security | 3.2.5 | 仅使用 `PasswordEncoder`（BCrypt），禁用过滤器链 |

### 目录结构

```
springboot-backend/src/main/java/com/netconfig/
├── NetConfigApplication.java       # 主入口（@SpringBootApplication + @EnableScheduling）
├── config/                         # 配置层（9 个文件）
│   ├── AuthInterceptor.java        # Token 鉴权拦截器（角色 + CSRF）
│   ├── CorsConfig.java             # CORS 跨域配置
│   ├── DatabaseInitializer.java    # 启动初始化（建表、默认账号、数据迁移）
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   ├── ProjectRootConfig.java      # 项目根路径解析
│   ├── RateLimitingFilter.java     # 登录限流过滤器（5 次/5 分钟/IP）
│   ├── SecurityConfig.java         # BCrypt PasswordEncoder bean
│   ├── SpaForwardController.java   # SPA 路由转发
│   └── WebConfig.java              # Web MVC 配置（拦截器注册、静态资源）
├── controller/                     # 控制器（15 个文件）
│   ├── AuthController.java         # 认证接口（登录/注册/用户管理）
│   ├── CommandController.java      # 网络命令 CRUD
│   ├── DesktopController.java      # 桌面运维 CRUD
│   ├── FaultController.java        # 故障排查 CRUD
│   ├── LinuxController.java        # Linux CRUD
│   ├── OfficeController.java       # Office CRUD
│   ├── AiTopicController.java      # AI 主题 CRUD
│   ├── LearningNoteController.java # 学习笔记/评论（含点赞、回复）
│   ├── FileUploadController.java   # 文件上传
│   ├── ClickController.java        # 点击统计（记录、统计、TOP10）
│   ├── CategoryController.java     # 分类标签管理
│   ├── SearchHistoryController.java# 搜索历史管理
│   ├── PendingChangeController.java# 审核变更流程
│   ├── FavoriteController.java     # 收藏管理（toggle/check/list）
│   └── UserProfileController.java  # 用户资料管理
├── dto/                            # 数据传输对象（7 个文件）
│   ├── ApiResponse.java            # 通用响应包装 {ok, data, error}
│   ├── CommandDTO.java             # 命令主题 DTO（含 ConfigItem 嵌套类）
│   ├── DesktopDTO.java             # 桌面运维 DTO
│   ├── FaultDTO.java               # 故障排查 DTO
│   ├── LinuxDTO.java               # Linux DTO（含 config/configs 双字段兼容）
│   ├── OfficeDTO.java              # Office DTO
│   └── AiTopicDTO.java             # AI 主题 DTO
├── entity/                         # JPA 实体（17 个文件）
│   ├── User.java                   # 用户表
│   ├── UserToken.java              # 登录 Token 表
│   ├── UserProfile.java            # 用户资料表
│   ├── CommandTopic.java           # 命令主题表
│   ├── CommandConfig.java          # 厂商配置表
│   ├── Fault.java                  # 故障表
│   ├── Desktop.java                # 桌面运维表
│   ├── Linux.java                  # Linux 表
│   ├── Office.java                 # Office 表
│   ├── AiTopic.java                # AI 主题表
│   ├── LearningNote.java           # 学习笔记表（评论）
│   ├── NoteReaction.java           # 笔记点赞/点踩记录
│   ├── ClickRecord.java            # 点击统计表
│   ├── SearchHistory.java          # 搜索历史表
│   ├── PendingChange.java          # 待审核变更表
│   ├── CategoryLabel.java          # 分类标签表
│   ├── Favorite.java               # 收藏记录表
│   └── CategoryExclusion.java      # 分类排除表
├── repository/                     # 数据仓库（18 个文件）
│   └── (每个 Entity 对应一个 Repository，继承 JpaRepository)
└── service/                        # 服务层（12 个文件）
    ├── AuthService.java            # 认证逻辑（注册/登录/Token 管理）
    ├── CommandService.java         # 命令主题业务（级联操作）
    ├── DesktopService.java         # 桌面运维业务
    ├── FaultService.java           # 故障排查业务
    ├── LinuxService.java           # Linux 业务
    ├── OfficeService.java          # Office 业务
    ├── AiTopicService.java         # AI 主题业务
    ├── LearningNoteService.java    # 笔记业务（点赞/回复/CRUD）
    ├── NoteModerationService.java  # 笔记内容审核（违禁词、正则、长度）
    ├── UserProfileService.java     # 用户资料业务
    ├── FavoriteService.java        # 收藏业务（toggle/check/list）
    └── JsonUtil.java               # JSON 工具 + 时间工具
```

### 安全机制

| 机制 | 实现 | 说明 |
|------|------|------|
| **Token 认证** | `AuthInterceptor` + `UserToken` 表 | 登录生成 64 位 hex token，请求需带 `Authorization: Bearer <token>` 头 |
| **CSRF 防护** | `AuthInterceptor` | 写操作需携带 `X-Requested-With: XMLHttpRequest` 头 |
| **登录限流** | `RateLimitingFilter` | 每 IP 每 5 分钟最多 5 次登录失败 |
| **密码加密** | BCrypt（`SecurityConfig`） | 兼容旧版 SHA-256，登录时自动迁移 |
| **密码过期** | Token 7 天过期 | `UserToken.expiresAt` 字段 |
| **内容审核** | `NoteModerationService` | 评论内容过滤 60+ 违禁词、URL、手机号、邮箱 |
| **文件验证** | `FileUploadController` | 扩展名白名单 + MIME 类型校验 |
| **权限校验** | `AuthInterceptor` | 三级角色矩阵控制 |

### 权限矩阵

| 操作 | 游客 | ADMIN（普通管理员） | SUPER_ADMIN（超级管理员） |
|------|------|------|------|
| 浏览所有页面 | ✅ | ✅ | ✅ |
| 发表评论 | ✅ | ✅ | ✅ |
| 新增/修改/删除内容 | ❌ | 需审核 | ✅（直接写入） |
| 管理用户 | ❌ | ❌ | ✅ |
| 审核变更 | ❌ | ❌ | ✅ |
| 文件上传 | ❌ | ✅ | ✅ |

---

## API 文档

### 认证

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| GET | `/api/auth/check-username` | 检查用户名是否可用 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 获取当前登录用户信息 |
| GET | `/api/auth/users` | 获取所有用户（SUPER_ADMIN） |
| POST | `/api/auth/approve/{userId}` | 审批用户（SUPER_ADMIN） |
| DELETE | `/api/auth/users/{userId}` | 删除用户（SUPER_ADMIN） |

### 业务 CRUD

每个业务模块（`topics`, `faults`, `desktop`, `linux`, `office`, `ai`）具有相同模式的 RESTful API：

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/{module}` | 列表查询 |
| GET | `/api/{module}/{id}` | 单条详情 |
| POST | `/api/{module}` | 新增 |
| PUT | `/api/{module}/{id}` | 更新 |
| DELETE | `/api/{module}/{id}` | 删除 |
| POST | `/api/{module}/batch-delete` | 批量删除 |

### 其他 API

| 方法 | 端点 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/learning-notes/**` | 学习笔记/评论 CRUD + 点赞/点踩 |
| POST | `/api/upload` | 文件上传 |
| POST/GET | `/api/clicks/record`, `/api/clicks/stats` | 点击记录与统计 |
| GET | `/api/clicks/top10`, `/api/clicks/top10/{module}` | 点击 TOP10 排行 |
| GET/POST/DELETE | `/api/categories/**` | 分类标签管理 |
| GET/POST/DELETE | `/api/search-history/**` | 搜索历史管理 |
| POST | `/api/admin/pending-change` | 提交待审核变更（ADMIN） |
| GET | `/api/admin/pending-changes` | 获取待审核列表（SUPER_ADMIN） |
| POST | `/api/admin/pending-change/{id}/approve` | 批准变更（SUPER_ADMIN） |
| POST | `/api/admin/pending-change/{id}/reject` | 拒绝变更（SUPER_ADMIN） |
| GET | `/api/favorites` | 获取收藏列表（登录） |
| POST | `/api/favorites/toggle` | 切换收藏/取消收藏（登录） |
| POST | `/api/favorites/check` | 检查是否已收藏（登录） |
| GET/POST | `/api/profile/**` | 用户个人资料 |

---

## 数据库

### 表结构（共 18 张表）

| 表名 | 实体 | 说明 |
|------|------|------|
| `users` | `User` | 用户账号（id, username, password, role, status, createdAt） |
| `user_tokens` | `UserToken` | 登录 Token（token, userId, role, expiresAt） |
| `user_profiles` | `UserProfile` | 用户资料（id, userId, realName, email, avatar, bio） |
| `command_topics` | `CommandTopic` | 网络命令主题（id, title, cat, desc, detail, topo, files） |
| `command_configs` | `CommandConfig` | 厂商配置（id, topicId, vendor, config, comment, doc, verificationCmd, verificationImages） |
| `faults` | `Fault` | 故障排查条目 |
| `desktop` | `Desktop` | 桌面运维条目 |
| `linux` | `Linux` | Linux 条目 |
| `office` | `Office` | Office 条目 |
| `ai_topics` | `AiTopic` | AI 主题条目 |
| `learning_notes` | `LearningNote` | 学习笔记/评论（id, targetId, username, content, likeCount, dislikeCount, parentId, createdAt） |
| `note_reactions` | `NoteReaction` | 笔记点赞/点踩记录（noteId, userId, reactionType） |
| `favorites` | `Favorite` | 收藏记录（userId, module, itemId, itemTitle, moduleLabel, description, category, itemPath, createdAt） |
| `click_records` | `ClickRecord` | 点击统计（module, itemId, itemTitle, count） |
| `search_history` | `SearchHistory` | 搜索历史（module, keyword, searchedAt） |
| `pending_changes` | `PendingChange` | 待审核变更（module, operation, entityId, payload, submitter, status） |
| `category_labels` | `CategoryLabel` | 分类标签（catKey, catLabel） |
| `category_exclusions` | `CategoryExclusion` | 分类排除（catKey） |

### 数据库特点

- **类型：** SQLite（嵌入式，无需安装数据库服务）
- **位置：** 项目根目录 `date.db`
- **DDL 策略：** Hibernate `ddl-auto=update` + `DatabaseInitializer` 启动时自动补全
- **日志模式：** WAL（Write-Ahead Logging），提升并发性能

---

## 权限体系

### 角色分级

| 角色 | 级别 | 说明 |
|------|------|------|
| `SUPER_ADMIN` | 3（最高） | 系统管理员，拥有全部权限，可直接写入数据 |
| `ADMIN` | 2 | 普通管理员，可提交变更但需要超级管理员审核 |
| `USER` | 1 | 预留普通用户角色（当前未使用） |
| 游客 | 0 | 未登录用户，仅可浏览 |

### 审核工作流

普通管理员（ADMIN）不能直接修改数据，流程如下：

```
ADMIN 提交变更 → PendingChange（status=PENDING）
                       ↓
            SUPER_ADMIN 审查变更
                       ↓
              ┌────────┴────────┐
              ▼                 ▼
        批准（APPROVED）    拒绝（REJECTED）
              ▼
        执行实际数据库操作
```

- **支持的操作：** CREATE、UPDATE、DELETE
- **支持的模块：** cmd, fault, desktop, linux, office, ai
- **审核界面：** `/super-admin` 页面提供变更对比视图（原始值 vs 新值）

---

## 操作指南

### 基本操作

**内容管理：** 登录后进入各模块管理页面（`/{module}/admin`），可新增、编辑、删除条目。

**分类管理：**
- 添加/编辑条目时，分类使用 `ComboBox` 组件，支持直接输入新分类（自动保存）
- 管理页面支持对已存在的分类进行编辑或删除
- 超级管理员可在 `/super-admin` 配置分类排除

**评论系统：** 每个知识条目底部有评论区，支持：
- 发表评论和回复
- 点赞/点踩
- 最热/最新排序
- 内容经过关键词和 URL 过滤审核

**文件上传：** `/api/upload` 接口支持 100MB 以下的多格式文件上传，允许类型包括：png、jpg、gif、pdf、doc、docx、xls、xlsx、zip 等 25+ 种格式。

**PDF 导出：** 详情页支持一键导出排版美观的 A4 PDF 文档，保留图片和代码高亮。

**搜索：** 每个模块列表页支持关键词搜索 + 厂商/分类多维筛选，搜索记录按模块独立保存。

**同类别内容面板：** 详情页侧边浮动面板，自动展示同分类的条目。支持：
- 可拖拽切换靠左/靠右/收起
- 面板收起后，右侧保留展开按钮
- 响应式：窗口宽度 < 1200px 时自动隐藏

### 默认管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | SUPER_ADMIN |

> 生产环境请立即修改默认密码。

---

## 技术细节

### 前端组件通信模式

```
Props 向下传递 + Events 向上冒泡
  父组件 ──props──→ 子组件
  子组件 ──emit──→ 父组件

全局状态：
  stores/auth.js          → 认证状态（Token/用户名/角色）
  composables/useSidebarCollapse.js → 侧边栏状态（折叠/位置，localStorage 持久化）
  utils/userLibrary.js    → 收藏（API 持久化，登录用户）/ 浏览历史（localStorage，跨组件事件通知）
```

### 后端关键设计

**级联删除：** 命令模块删除主题时，自动级联删除关联的厂商配置、学习笔记、点击记录。其他模块删除时同步清理点击记录。

**密码迁移：** 支持旧版 SHA-256 密码到 BCrypt 的无缝迁移。用户登录时若检测到旧版密码，自动计算哈希并迁移。

**Token 管理：** 不使用 JWT，采用随机 64 位 hex 字符串作为 Token，存储于 `user_tokens` 表，7 天过期。登出时直接从数据库删除。

**评论审核：** `NoteModerationService` 对评论内容进行三层过滤：违禁关键词（60+）、正则模式（URL、手机号、邮箱）、长度限制（2000 字）。

**SPA 路由转发：** `SpaForwardController` 将非 API 路径的前端路由转发到 `index.html`，后端支持直接托管前端构建产物。

### 数据校验

- **前端：** `required` 属性、`@NotBlank` 校验、`ComboBox` 必填检查
- **后端：** `@Valid @RequestBody` + `@NotBlank @Size(max=200)` 注解校验
- **审核：** `PendingChange` 中存储完整 JSON 负载，批准时校验格式

### 启动时自动操作（DatabaseInitializer）

1. 创建 17 张表（`CREATE TABLE IF NOT EXISTS`）
2. 初始化超级管理员账号（密码 BCrypt 加密）
3. 清理废弃表（`commands`, `meta`）
4. 补充缺失列（如 `images`, `videos`, `dislike_count`）
5. 修复空值和日期格式
6. 设置 PRAGMA（WAL 模式、外键约束）

---

## 开发指南

### 项目约定

- **前端：** Vue 3 Composition API + `<script setup>`，Tailwind CSS 原子类 + 自定义全局样式
- **后端：** Spring Boot 3 + Lombok + JPA，控制器层轻薄，业务逻辑在 Service 层
- **数据库迁移：** 不单独管理迁移文件，依赖 `ddl-auto=update` + `DatabaseInitializer` 启动时自动执行
- **命名规范：** API 路径使用小写/复数/kebab-case；数据库表使用 snake_case；Java 类使用 PascalCase；Vue 文件使用 PascalCase

### 新增模块步骤

1. **后端：** 创建 Entity → Repository → Service → DTO → Controller → 在 `AuthInterceptor` 添加新模块的权限规则 → 在 `DatabaseInitializer` 添加建表语句
2. **前端：** 创建 5 个页面（List/Detail/Admin/Add/Edit）→ 在 `api/modules.js` 添加 API 端点 → 在 `router/index.js` 添加路由 → 在 `Sidebar.vue` 添加菜单项
3. **其他：** 在 `ClickController` 的 `MODULES` 数组添加模块名 → 在主页 `HomePage.vue` 添加模块卡片

### 构建命令

```powershell
# 前端构建
cd vue-frontend
npm run build     # 输出到 dist/

# 后端构建
cd springboot-backend
mvn clean package -DskipTests   # 生成 target/netconfig-backend-1.0.0.jar
```

---

## 部署

### 生产部署（单机）

```powershell
# 运行后端
java -jar springboot-backend/target/netconfig-backend-1.0.0.jar

# 前端 dist/ 部署到 Nginx
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/netconfig/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads/ {
        proxy_pass http://localhost:8080;
    }
}
```

### 健康检查

```powershell
# 检查后端
Invoke-WebRequest -Uri http://localhost:8080/api/auth/me

# 检查前端
Invoke-WebRequest -Uri http://localhost:3000
```

---

## 项目统计

| 维度 | 数据 |
|------|------|
| 后端 Java 源码文件 | 54 个 |
| 前端 Vue/JS 源码文件 | 58 个 |
| 数据库表 | 18 张 |
| 前端共享组件 | 13 个 |
| 页面组件 | 30 个 |
| RESTful API 端点 | 55+ 个 |
| 路由 | 31 条 |
| 全局 CSS 行数 | 1090+ 行（含移动端适配） |
| 移动端适配页面 | 全覆盖（768px 及以下） |

---

## 许可证

Private & Internal Use Only.

---

## 最后更新

2026-06-13（v2.0 — 收藏功能数据库持久化）

---

## 移动端适配说明

- 全站页面已通过移动端媒体查询适配 768px 及以下视口，桌面端原有宽度、留白和交互保持不变。
- 覆盖范围：主布局、首页、列表页、详情页、添加/编辑表单、管理表格、个人中心、超级管理员页、搜索框和下拉筛选组件。
- 管理表格在小屏幕保留横向滚动，避免压缩字段导致内容重叠或误触。
- 详情页、评论区、同类别内容框、收藏/历史卡片在移动端使用单列或自适应布局。
