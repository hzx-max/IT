# 全局 AI 代理指令

## 语言

- 始终使用**简体中文**回复用户。
- 代码注释、提交信息、文档、变量命名等产物在不影响技术规范的前提下优先使用中文。
- 专有名词（如 API、URL、Token、JSON、CSS、Spring Boot、Vue、Maven、PowerShell 等）保留英文原文，不要音译。
- 当用户用英文提问时，也用中文回复，除非用户明确要求英文。
- 引述错误信息或日志时，保留原始英文以方便排查，但可在后面用中文简要解释含义。

## 风格

- 简洁、直接，避免冗长的开场白和总结。
- 优先用文件路径 + 行号（如 `src/foo.ts:42`）定位代码。
- 修改文件前先简要说明要做什么（1-2 句），不要解释显而易见的操作。
- 用户要求回退/撤销时，立即执行，不附加"你确定吗"之类的确认。
- 不要主动添加 emoji。
- 不要在代码或配置文件中添加注释，除非用户明确要求。

## 工作流

- 处理项目前，先阅读项目根目录 `README.md` 及其他 `.md` 文件了解项目背景、技术栈和运行方式。
- 复杂任务（≥3 步）使用 `todowrite` 工具拆解并跟踪进度。
- 修改前先 `read` 文件，理解上下文后再 `edit`。
- 多个独立操作尽量并行调用工具。
- 完成任务后，除非用户要求，不要主动 commit。
- 涉及系统级操作（环境变量、服务启停、PATH 修改）时，先用只读命令（`Get-ChildItem`、`where` 等）探查，再给出明确指令。
- **修改样式或功能前**，先评估改动对其他模块和全局样式的影响，避免破坏已有功能或视觉一致性。
- **修改或增加功能后必须测试**：通过 API 调用验证功能是否正常；若改动可能影响其他功能，测试范围需覆盖受影响模块。
- 涉及数据库 schema 变更时，验证 Hibernate 自动建表是否正确，并检查旧数据兼容性。

## 备份与修改记录

1. **数据库备份** — 涉及 `D:\桌面\IT运维学习平台\date.db` 的任何修改前，必须先复制到 `date.db.bak`。
2. **项目全量备份** — 修改项目前，将整个项目目录 `D:\桌面\IT运维学习平台` 复制到 `backup\IT运维学习平台_yyyyMMdd_HHmmss`（如 `backup\IT运维学习平台_20260611_110000`）。
3. **修改记录文件** — 项目根目录 `D:\桌面\IT运维学习平台\修改记录.txt` 必须维护，每次修改完成后追加条目，格式如下：
   ```
   ======== yyyy-MM-dd HH:mm:ss ========
   操作人：AI（opencode）
   修改内容：（简要描述改了哪些文件、原因）
   影响范围：（可能影响哪些功能）
   ```

## 启动项目

启动项目时，后端和前端必须在**后台静默运行**（不打开新窗口），使用 `Start-Process -WindowStyle Hidden`：

```powershell
# 后端（端口 8080）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location -LiteralPath 'D:\桌面\IT运维学习平台\springboot-backend'; .\mvnw spring-boot:run" -WindowStyle Hidden

# 前端（端口 3000）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location -LiteralPath 'D:\桌面\IT运维学习平台\vue-frontend'; npm run dev" -WindowStyle Hidden
```

查看运行状态：`netstat -ano | Select-String ":8080|:3000" | Select-String "LISTENING"`
查看 PID 对应进程：`Get-Process -Id <PID>`
