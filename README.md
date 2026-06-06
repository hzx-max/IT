# IT 运维学习平台

> 涵盖网络命令、网络故障、桌面运维、Linux、Office、AI 运维等 IT 运维核心知识的综合学习与管理系统。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen.svg)](https://vuejs.org/)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-Private-lightgrey.svg)](#)

---

## 📑 目录

- [项目简介](#-项目简介)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [目录结构](#-目录结构)
- [核心功能](#-核心功能)
- [数据概览](#-数据概览)
- [权限模型](#-权限模型)
- [审核工作流](#-审核工作流)
- [API 接口一览](#-api-接口一览)
- [数据库](#-数据库)
- [环境要求](#-环境要求)
- [快速启动](#-快速启动)
- [默认账号](#-默认账号)
- [快捷键](#-快捷键)
- [常见问题](#-常见问题)

---

## 📌 项目简介

**IT 运维学习平台**（NetConfig）是一个面向 IT 运维工程师的综合学习与知识管理平台，针对 6 大 IT 运维核心领域提供：

- 📚 **结构化知识库**：每条知识都包含描述、详细内容、拓扑图、配置命令、参考文档、验证命令、验证截图、附件等
- 🔍 **多维筛选**：按模块、厂商、分类、关键字组合搜索
- 📊 **数据可视化**：首页展示知识库总量、模块分布、点击量柱状图、TOP10 排行
- 🛡️ **审核机制**：普通管理员提交变更需超级管理员审核，确保数据安全
- 👥 **多角色管理**：游客 / 普通管理员 / 超级管理员三级权限
- 📤 **PDF 导出**：详情页一键导出排版美观的 PDF 文档
- 📝 **个人笔记**：每条命令支持跨设备同步笔记
- 🔎 **搜索历史**：每模块独立保留最近 5 条搜索历史

---

## 🛠 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 渐进式 UI 框架 |
| Vue Router | 4.3+ | 单页面路由 |
| Vite | 5.4+ | 构建工具 / 开发服务器 |
| Tailwind CSS | 3.4+ | 原子化样式 |
| Axios | 1.7+ | HTTP 请求 |
| html2pdf.js | 0.10.1 | PDF 导出（CDN 加载） |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | Web 框架 |
| Spring Data JPA | 3.2.5 | ORM |
| Spring Security | 3.2.5 | 安全框架 |
| Hibernate | 6.x | JPA 实现 |
| SQLite JDBC | 3.45.1.0 | 数据库驱动 |
| Hibernate Community Dialects | - | SQLite 方言 |
| Lombok | - | 简化 Java 代码 |
| Jackson | - | JSON 序列化 |
| Jakarta Servlet API | 6.0 | Servlet 规范 |

### 数据库

| 类型 | 文件 | 用途 |
|------|------|------|
| SQLite | `date.db` | 嵌入式数据库（零配置） |

### 工具链

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行时 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端依赖管理 |

---

## 🏗 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                        用户浏览器                              │
│              http://localhost:3000                            │
└──────────────────────────┬───────────────────────────────────┘
                           │ (HTTP)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│              Vue 3 SPA  (Vite Dev Server)                    │
│  - 路由 (vue-router)                                          │
│  - 状态 (localStorage + composables)                          │
│  - 组件 (Sidebar / SearchBar / FileUploader ...)              │
└──────────────────────────┬───────────────────────────────────┘
                           │ /api/* (Axios)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│           Spring Boot  (Tomcat :8080)                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Filter: RateLimitingFilter（登录限流）              │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Interceptor: AuthInterceptor（Token 鉴权）          │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Controllers  →  Services  →  Repositories          │    │
│  │  (REST API)    (业务逻辑)     (JPA 数据访问)          │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  DatabaseInitializer（应用启动时建表 + 默认数据）    │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────┬───────────────────────────────────┘
                           │ JDBC
                           ▼
                  ┌──────────────────┐
                  │  SQLite: date.db │
                  └──────────────────┘
                           ▲
                           │
┌──────────────────────────┴───────────────────────────────────┐
│              /uploads/* （静态文件，由 WebConfig 暴露）       │
└──────────────────────────────────────────────────────────────┘
```

---

## 📂 目录结构

```
IT运维学习平台/
├── date.db                    # SQLite 数据库（自动生成）
├── date.db-shm                # SQLite WAL 模式共享内存
├── date.db-wal                # SQLite WAL 模式日志
│
├── springboot-backend/        # 后端工程
│   ├── pom.xml                # Maven 配置
│   ├── mvnw / mvnw.cmd        # Maven Wrapper
│   ├── start.bat / start.sh   # 启动脚本
│   ├── uploads/               # 用户上传文件
│   └── src/main/
│       ├── java/com/netconfig/
│       │   ├── NetConfigApplication.java    # SpringBoot 启动类
│       │   ├── config/                       # 配置
│       │   │   ├── CorsConfig.java
│       │   │   ├── SecurityConfig.java
│       │   │   ├── AuthInterceptor.java      # 鉴权拦截器
│       │   │   ├── RateLimitingFilter.java   # 登录限流
│       │   │   ├── DatabaseInitializer.java   # 建表 + 默认数据
│       │   │   ├── WebConfig.java            # 静态资源映射
│       │   │   ├── SpaForwardController.java # SPA fallback
│       │   │   ├── ProjectRootConfig.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── controller/                   # REST 控制器
│       │   │   ├── AuthController.java
│       │   │   ├── CommandController.java    # 网络命令
│       │   │   ├── FaultController.java      # 网络故障
│       │   │   ├── DesktopController.java    # 桌面运维
│       │   │   ├── LinuxController.java      # Linux
│       │   │   ├── OfficeController.java     # Office
│       │   │   ├── AiTopicController.java    # AI 运维
│       │   │   ├── CategoryController.java   # 分类管理
│       │   │   ├── NoteController.java       # 笔记
│       │   │   ├── ClickController.java      # 点击统计
│       │   │   ├── SearchHistoryController.java
│       │   │   ├── FileUploadController.java
│       │   │   └── PendingChangeController.java # 审核
│       │   ├── service/                      # 业务层
│       │   │   ├── AuthService.java
│       │   │   ├── CommandService.java
│       │   │   ├── FaultService.java
│       │   │   ├── DesktopService.java
│       │   │   ├── LinuxService.java
│       │   │   ├── OfficeService.java
│       │   │   ├── AiTopicService.java
│       │   │   ├── NoteService.java
│       │   │   └── JsonUtil.java
│       │   ├── repository/                   # JPA 仓储
│       │   │   ├── UserRepository.java
│       │   │   ├── UserTokenRepository.java
│       │   │   ├── PendingChangeRepository.java
│       │   │   ├── ClickRecordRepository.java
│       │   │   ├── SearchHistoryRepository.java
│       │   │   ├── NoteRepository.java
│       │   │   ├── CommandTopicRepository.java
│       │   │   ├── CommandConfigRepository.java
│       │   │   ├── CategoryLabelRepository.java
│       │   │   ├── CategoryExclusionRepository.java
│       │   │   └── （每个模块一个 Repository）
│       │   ├── entity/                       # JPA 实体
│       │   │   ├── User.java
│       │   │   ├── UserToken.java
│       │   │   ├── PendingChange.java
│       │   │   ├── ClickRecord.java
│       │   │   ├── SearchHistory.java
│       │   │   ├── Note.java
│       │   │   ├── CommandTopic.java
│       │   │   ├── CommandConfig.java
│       │   │   ├── CategoryLabel.java
│       │   │   ├── CategoryExclusion.java
│       │   │   ├── Fault.java
│       │   │   ├── Desktop.java
│       │   │   ├── Linux.java
│       │   │   ├── Office.java
│       │   │   └── AiTopic.java
│       │   └── dto/                          # 数据传输对象
│       │       ├── ApiResponse.java          # 统一响应
│       │       ├── CommandDTO.java
│       │       ├── FaultDTO.java
│       │       ├── DesktopDTO.java
│       │       ├── LinuxDTO.java
│       │       ├── OfficeDTO.java
│       │       └── AiTopicDTO.java
│       └── resources/
│           ├── application.properties        # SpringBoot 配置
│           └── static/                       # 静态文件目录
│
├── vue-frontend/              # 前端工程
│   ├── package.json           # npm 配置
│   ├── vite.config.js         # Vite 配置（含 /api 代理）
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   └── src/
│       ├── main.js            # 入口
│       ├── App.vue            # 根组件
│       ├── api/               # API 层
│       │   ├── http.js        # Axios 实例
│       │   ├── modules.js     # 各模块 API
│       │   ├── approval.js    # 审核流封装
│       │   └── constants.js   # 厂商/分类/格式化常量
│       ├── assets/            # 静态资源
│       │   └── main.css       # 全局样式
│       ├── components/        # 通用组件
│       │   ├── Sidebar.vue
│       │   ├── NavDropdown.vue
│       │   ├── SearchBar.vue
│       │   ├── CategoryStrip.vue
│       │   ├── CardCarousel.vue
│       │   ├── FileUploader.vue
│       │   ├── ComboBox.vue
│       │   ├── ModalDialog.vue
│       │   └── ToastMessage.vue
│       ├── composables/       # 组合式函数
│       ├── layouts/           # 布局
│       │   └── MainLayout.vue
│       ├── pages/             # 页面
│       │   ├── HomePage.vue   # 首页（数据概览）
│       │   ├── auth/          # 认证模块
│       │   │   ├── LoginPage.vue
│       │   │   ├── RegisterPage.vue
│       │   │   └── SuperAdminPage.vue
│       │   ├── cmd/           # 网络命令
│       │   │   ├── CmdList.vue
│       │   │   ├── CmdDetail.vue
│       │   │   ├── CmdAdmin.vue
│       │   │   ├── CmdAdd.vue
│       │   │   └── CmdEdit.vue
│       │   ├── fault/         # 网络故障（同上 5 个）
│       │   ├── desktop/       # 桌面运维（同上 5 个）
│       │   ├── linux/         # Linux（同上 5 个）
│       │   ├── office/        # Office（同上 5 个）
│       │   └── ai/            # AI 运维（同上 5 个）
│       ├── router/
│       │   └── index.js       # 路由 + 守卫
│       ├── stores/
│       │   └── auth.js        # 认证状态（localStorage）
│       └── utils/
│           └── pdfExport.js   # PDF 导出工具
│
└── uploads/                   # 上传文件副本目录（开发环境）
```

---

## 🎯 核心功能

### 🏠 首页

- 6 大模块入口卡片（带渐变图标）
- 数据概览面板
  - 知识库总量
  - 各模块条目数（点击跳转）
  - 各模块点击量竖直柱状图
  - 6 个 TOP10 排行面板
- 登录态显示用户信息、角色徽章

### 🔐 认证模块

| 功能 | 描述 |
|------|------|
| 注册 | 提交后状态为 `PENDING`，需超级管理员审批 |
| 登录 | 返回 Token + username + role，写入 localStorage |
| 退出 | 删除 localStorage，跳转首页 |
| 登录限流 | 同 IP 5 分钟内最多 5 次失败尝试 |
| 个人信息 | `GET /api/auth/me` |

### 📚 6 大知识模块

每个模块都包含以下页面：

| 页面 | 路径 | 功能 |
|------|------|------|
| 列表 | `/{module}` | 卡片网格 + 搜索 + 厂商/分类筛选 + 分类标签条 |
| 详情 | `/{module}/detail/:id` | 完整内容 + 多厂商配置 + 拓扑图 + 笔记 + PDF 导出 |
| 管理 | `/{module}/admin` | 表格 + 批量选择 + 批量删除 + 行内操作 |
| 新增 | `/{module}/add` | 表单（多厂商配置 + 文件上传 + 富文本） |
| 编辑 | `/{module}/edit/:id` | 同上，预填数据 |

#### 模块说明

| 模块 | 路径 | 特色 |
|------|------|------|
| **网络命令** | `/cmd` | 多厂商（华为/H3C/Cisco/锐捷），每家含配置命令/说明/参考文档/验证命令/验证截图 |
| **网络故障** | `/fault` | 故障现象/原因分析/解决方案 |
| **桌面运维** | `/desktop` | 桌面系统常见问题 |
| **Linux** | `/linux` | 多发行版（CentOS/Ubuntu/Debian） |
| **Office** | `/office` | Word/Excel/PPT 操作 |
| **AI 运维** | `/ai` | ChatGPT/Copilot/Claude 应用 |

### 🛠 通用功能

| 功能 | 描述 |
|------|------|
| 搜索 | 支持标题/描述模糊搜索 |
| 厂商筛选 | 各模块独立的厂商下拉 |
| 分类筛选 | 分类下拉 + 横向标签条 |
| 搜索历史 | 每模块最多 5 条，独立保存 |
| 多图轮播 | 列表卡片展示拓扑图 |
| 拓扑图/截图 | 详情页网格 + 点击放大 |
| 附件管理 | 多格式文件上传/下载 |
| 个人笔记 | 每条命令独立笔记，跨设备同步 |
| PDF 导出 | 一键导出详情页为 PDF |
| 点击量统计 | 访问详情页自动累加 |

### 🏷️ 分类管理

- 分类键值对（key → label）
- 分类排除（从筛选中隐藏特定分类）

### 📊 点击量统计

- 自动累计每个 module + itemId 的访问次数
- 实时显示各模块总点击量
- 全站 TOP10 + 每模块 TOP10
- 自动清理孤立记录（引用的数据删除后点击记录自动清除）

### 📤 文件上传

- 路径：`POST /api/upload`
- 大小限制：100MB
- 支持格式：图片 / PDF / Word / Excel / PPT / TXT / Markdown / CSV / ZIP / RAR / 7Z / TAR / GZ
- 双重校验：扩展名 + MIME 前缀
- 文件 URL：`/uploads/{uuid}.{ext}`

---

## 📈 数据概览

首页数据概览面板展示：

```
┌─────────────────────────────────────────────────────┐
│  数据概览          知识库总量：1234 条                 │
├─────────────────────────────────────────────────────┤
│  [网络命令 100] [网络故障 200] [桌面运维 150]         │
│  [Linux 300]    [Office 250]   [AI运维 234]          │
├─────────────────────────────────────────────────────┤
│  各模块点击量分布                                    │
│  ▇▇▇▇  命令                                          │
│  ▇▇▇   故障                                          │
│  ▇▇    桌面                                          │
│  ...                                                 │
├─────────────────────────────────────────────────────┤
│  6 个 TOP10 排行（每个模块独立）                      │
│  #1 标题... 123次                                    │
│  #2 标题... 100次                                    │
└─────────────────────────────────────────────────────┘
```

---

## 🛡️ 权限模型

### 三级角色

| 角色 | 代号 | 权限 |
|------|------|------|
| 游客 | - | 浏览所有公开内容 |
| 普通管理员 | `ADMIN` | 浏览 + 提交增删改（需审核）+ 编辑笔记 |
| 超级管理员 | `SUPER_ADMIN` | 全部权限 + 直接执行 + 审核 + 用户管理 |

### 鉴权流程

```
请求 → RateLimitingFilter
     → AuthInterceptor (Token 验证)
     → 路由分发
     → Controller
     → Service
     → Repository
     → SQLite
```

### 接口权限

| 接口前缀 | 权限要求 |
|----------|----------|
| `GET /api/auth/login`、`register` | 公开 |
| `GET /api/clicks/*` | 公开 |
| `POST /api/auth/logout` | 需登录 |
| `GET /api/auth/me` | 需登录 |
| `GET /api/auth/users` | 需 SUPER_ADMIN |
| `POST /api/auth/approve/*` | 需 SUPER_ADMIN |
| `DELETE /api/auth/users/*` | 需 SUPER_ADMIN |
| `POST /api/admin/pending-change` | 需 ADMIN 或 SUPER_ADMIN |
| `GET /api/admin/pending-changes` | 需 SUPER_ADMIN |
| `POST /api/admin/pending-change/*/approve\|reject` | 需 SUPER_ADMIN |
| `GET /api/*/{module}`（读） | 公开 |
| `POST/PUT/DELETE /api/*/{module}`（写） | 需 ADMIN 或 SUPER_ADMIN |

---

## ✅ 审核工作流

**核心特色**：普通管理员的写操作不直接入库，而是进入待审核队列。

```
普通管理员 ADMIN
  │
  ├─→ 在 /cmd/add 提交新增表单
  │
  ▼
submitWithApproval(module, 'CREATE', payload, null, directApiCall)
  │
  ├─ 角色 = SUPER_ADMIN → 直接调用 directApiCall() 立即生效
  │
  └─ 角色 = ADMIN       → POST /api/admin/pending-change
                              │
                              ▼
                       pending_changes 表新增记录
                       (PENDING 状态)
                              │
                              ▼
                       超级管理员在 /super-admin 看到列表
                              │
                              ├─ 批准 → POST /api/admin/pending-change/{id}/approve
                              │         → executeChange() → 调用对应 Service
                              │         → 改状态为 APPROVED
                              │
                              └─ 拒绝 → POST /api/admin/pending-change/{id}/reject
                                        → 改状态为 REJECTED
```

### 变更记录字段

| 字段 | 说明 |
|------|------|
| `id` | 自增主键 |
| `module` | 模块名（cmd / fault / desktop / linux / office / ai） |
| `operation` | 操作（CREATE / UPDATE / DELETE） |
| `entityId` | 实体 ID（CREATE 时可空） |
| `payload` | 完整请求数据（JSON 字符串） |
| `submitterId` | 提交人 ID |
| `submitterName` | 提交人用户名 |
| `status` | 状态（PENDING / APPROVED / REJECTED） |
| `createdAt` | 提交时间 |
| `approvedAt` | 审批时间 |
| `approvedBy` | 审批人 |

---

## 🔌 API 接口一览

> 所有响应统一格式：`{ "ok": boolean, "data": T, "error": string, "msg": string }`

### 认证

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | `/api/auth/register` | 公开 | 注册（待审核） |
| POST | `/api/auth/login` | 公开 | 登录（限流 5次/5分钟） |
| POST | `/api/auth/logout` | 登录 | 退出 |
| GET | `/api/auth/me` | 登录 | 当前用户 |
| GET | `/api/auth/users` | SUPER_ADMIN | 全部用户 |
| POST | `/api/auth/approve/{userId}` | SUPER_ADMIN | 审批用户 |
| DELETE | `/api/auth/users/{userId}` | SUPER_ADMIN | 删除用户 |

### 知识模块（6 个模块同构）

`{module}` ∈ { `topics`, `faults`, `desktop`, `linux`, `office`, `ai` }

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | `/api/{module}` | 公开 | 列表 |
| GET | `/api/{module}/{id}` | 公开 | 详情 |
| POST | `/api/{module}` | 管理员 | 新增 |
| PUT | `/api/{module}/{id}` | 管理员 | 更新 |
| DELETE | `/api/{module}/{id}` | 管理员 | 删除 |
| POST | `/api/{module}/batch-delete` | 管理员 | 批量删除 |

### 笔记

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | `/api/notes/{cmdId}` | 登录 | 读取笔记 |
| PUT | `/api/notes/{cmdId}` | 登录 | 保存笔记 |

### 分类

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | `/api/categories` | 公开 | 全部分类标签 |
| POST | `/api/categories` | 管理员 | 新增/更新 |
| DELETE | `/api/categories/{key}` | 管理员 | 删除 |
| GET | `/api/categories/exclusions` | 公开 | 排除列表 |
| POST | `/api/categories/exclusions` | 管理员 | 添加排除 |
| DELETE | `/api/categories/exclusions/{key}` | 管理员 | 移除排除 |

### 点击统计

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | `/api/clicks/record` | 公开 | 记录点击 |
| GET | `/api/clicks/stats` | 公开 | 各模块总点击量 |
| GET | `/api/clicks/top10` | 公开 | 全站 TOP10 |
| GET | `/api/clicks/top10/{module}` | 公开 | 单模块 TOP10 |

### 搜索历史

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| GET | `/api/search-history/{module}` | 登录 | 历史列表（最近 5 条） |
| POST | `/api/search-history` | 登录 | 保存历史（去重） |
| DELETE | `/api/search-history/{module}/{id}` | 登录 | 单条删除 |
| DELETE | `/api/search-history/{module}` | 登录 | 清空模块历史 |

### 审核

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | `/api/admin/pending-change` | 管理员 | 提交待审核变更 |
| GET | `/api/admin/pending-changes` | SUPER_ADMIN | 待审核列表 |
| POST | `/api/admin/pending-change/{id}/approve` | SUPER_ADMIN | 批准 |
| POST | `/api/admin/pending-change/{id}/reject` | SUPER_ADMIN | 拒绝 |

### 文件

| 方法 | 路径 | 权限 | 描述 |
|------|------|------|------|
| POST | `/api/upload` | 登录 | 上传文件（≤100MB） |
| GET | `/uploads/{filename}` | 公开 | 访问上传文件 |

---

## 🗄 数据库

SQLite 数据库，启动时自动建表（`ddl-auto=update`）。

### 表结构

| 表名 | 主要字段 |
|------|----------|
| `users` | id, username, password, role, status, created_at |
| `user_tokens` | id, user_id, token, expired_at |
| `command_topics` | id, title, cat, topo(json), desc, detail, created_at |
| `command_configs` | id, topic_id, vendor, config, comment, doc, verification_cmd, verification_images(json) |
| `notes` | cmd_id(PK), content |
| `category_labels` | cat_key(PK), cat_label |
| `category_exclusions` | cat_key(PK) |
| `faults` | id, title, cat, desc, detail, files(json), created_at |
| `desktops` | 同上 |
| `linux` | id, title, vendor, cat, desc, detail, files(json), created_at |
| `office` | id, title, cat, desc, detail, files(json), created_at |
| `ai_topics` | id, title, cat, desc, detail, files(json), created_at |
| `click_records` | id, module, item_id, item_title, count |
| `search_history` | id, module, keyword, searched_at |
| `pending_changes` | id, module, operation, entity_id, payload(json), submitter_id, submitter_name, status, created_at, approved_at, approved_by |

---

## ⚙️ 环境要求

| 工具 | 最低版本 | 推荐版本 | 说明 |
|------|----------|----------|------|
| **JDK** | 17 | 21 LTS | Spring Boot 3.2 要求 |
| **Maven** | 3.6 | 3.9+ | 后端构建 |
| **Node.js** | 18 | 20 LTS | 前端构建 |
| **npm** | 9 | 10+ | 前端依赖管理 |
| **磁盘空间** | 2GB | - | 包含依赖与上传文件 |
| **内存** | 2GB | 4GB+ | - |

> **注意**：SQLite 数据库文件 `date.db` 会在首次启动时自动创建。

---

## 🚀 快速启动

### 方式一：使用启动脚本（推荐 Windows）

#### 1. 安装前置环境

```powershell
# 验证 Java
java -version

# 验证 Maven
mvn -version

# 验证 Node.js
node -v
npm -v
```

#### 2. 启动后端

双击运行 `springboot-backend/start.bat`，或在终端执行：

```powershell
cd springboot-backend
mvn spring-boot:run
```

后端启动后访问 http://localhost:8080

#### 3. 启动前端（新开一个终端）

```powershell
cd vue-frontend
npm install   # 首次运行需要安装依赖
npm run dev
```

前端启动后访问 http://localhost:3000

### 方式二：使用预编译 JAR

```powershell
# 后端
cd springboot-backend
mvn clean package -DskipTests
java -jar target/netconfig-backend-1.0.0.jar
```

### 方式三：生产构建

```powershell
# 前端打包
cd vue-frontend
npm run build   # 输出到 dist/

# 后端打包
cd ../springboot-backend
mvn clean package
```

### 端口说明

| 端口 | 服务 | 备注 |
|------|------|------|
| 3000 | Vue 前端（Vite Dev） | `vite.config.js` 中 `server.port: 3000` |
| 8080 | Spring Boot 后端 | `application.properties` 中 `server.port=8080` |

### 跨域处理

开发环境由 Vite 代理处理（`/api/*` 和 `/uploads/*` 代理到后端），生产环境需要 Nginx 反向代理或后端 `CorsConfig` 配置。

---

## 👤 默认账号

应用首次启动时会在 `users` 表创建默认超级管理员：

| 用户名 | 密码 | 角色 | 状态 |
|--------|------|------|------|
| `admin` | `admin123` | `SUPER_ADMIN` | `APPROVED` |

> ⚠️ **生产环境请立即修改默认密码！**

---

## ⌨️ 快捷键

| 快捷键 | 功能 |
|--------|------|
| `ESC` | 关闭移动端侧边栏 |
| `Ctrl+K` | 搜索（提示在侧边栏底部） |

---

## ❓ 常见问题

### Q1: 启动后端报 `mvn: command not found`

A: Maven 未安装或未配置到 PATH。

```powershell
# 验证 Maven
mvn -v

# 如果未安装，下载 Maven 3.9+ 并将 bin 目录加入系统 PATH
# 或在 IntelliJ IDEA 中直接运行 SpringBootApplication
```

### Q2: 启动后端报 `java: command not found`

A: JDK 未安装或未配置到 PATH。

```powershell
# 验证 Java
java -version

# 需要 JDK 17+，推荐 Eclipse Temurin 或 Oracle JDK
# 安装后设置 JAVA_HOME 和 PATH：
# JAVA_HOME=C:\jdk17\jdk-17.0.19+10
# PATH=%JAVA_HOME%\bin;...
```

### Q3: 端口 8080 已被占用

A: 编辑 `springboot-backend/src/main/resources/application.properties`：

```properties
server.port=9090
```

同时修改 `vue-frontend/vite.config.js` 中的代理目标。

### Q4: 前端请求跨域错误

A: 开发环境已通过 Vite 代理处理。如仍有跨域问题，检查 `vite.config.js`：

```js
server: {
  port: 3000,
  proxy: {
    '/api': { target: 'http://localhost:8080', changeOrigin: true },
    '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
  }
}
```

### Q5: 数据库锁定 / 损坏

A: 删除 `date.db*` 三个文件（保留前先备份），重启后端会自动重建。

### Q6: 文件上传失败

A: 检查 `application.properties` 中的上传大小限制：

```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Q7: 修改默认管理员密码

A: 当前版本无内置修改密码 UI。可直接操作数据库：

```sql
-- 密码已用 BCrypt 加密，建议通过代码生成
UPDATE users SET password = '<bcrypt-hash>' WHERE username = 'admin';
```

---

## 📦 部署建议

### 简易部署（单机）

```
1. 后端：mvn package 后用 java -jar 运行
2. 前端：npm run build 后将 dist/ 部署到 Nginx
3. Nginx 配置 /api/* 反代到 localhost:8080
4. Nginx 配置 /uploads/* 反代到 localhost:8080
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

### Docker 化（待实现）

可参考 `Dockerfile` + `docker-compose.yml` 将前后端及 SQLite 持久化卷打包。

---

## 🔧 开发提示

### 添加新模块

1. **后端**：
   - 创建 `Entity`（如 `NewModule.java`）
   - 创建 `Repository`（如 `NewModuleRepository.java`）
   - 创建 `DTO`（如 `NewModuleDTO.java`）
   - 创建 `Service`（如 `NewModuleService.java`）
   - 创建 `Controller`（如 `NewModuleController.java`，路由 `/api/newmodule`）
   - 在 `PendingChangeController.executeChange/doCreate/doUpdate/doDelete` 添加分支
   - 在 `DatabaseInitializer.run` 添加建表 SQL

2. **前端**：
   - 在 `vue-frontend/src/api/modules.js` 添加 `apiNewModule`
   - 创建 5 个页面：`NewModuleList / NewModuleDetail / NewModuleAdmin / NewModuleAdd / NewModuleEdit`
   - 在 `router/index.js` 添加路由
   - 在 `Sidebar.vue` 添加菜单项
   - 在 `HomePage.vue` 添加入口卡片

### 修改主题色

编辑 `vue-frontend/src/assets/main.css`，修改 CSS 变量：

```css
:root {
  --primary: #2563eb;
  --orange: #ea580c;
  /* ... */
}
```

---

## 📝 License

Private & Internal Use Only.

---

## 🤝 贡献

内部项目，如需贡献请联系项目负责人。

---

**最后更新**：2026
