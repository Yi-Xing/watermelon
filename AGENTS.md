# Watermelon 项目指令

本文件适用于仓库根目录及全部子目录。

## 工作流

- 处理本仓库的 Java 开发、诊断、重构、审查、构建或测试任务前，完整读取并遵循 `.agents/skills/water-java-projects/SKILL.md`。
- 跨仓库依赖、模块边界、Maven 验证顺序和通用交付要求以 `$water-java-projects` 为准；本文只补充 Watermelon 特有约定。

## 仓库职责

Watermelon 是用户与权限服务：

- `watermelon-api`：跨服务 Dubbo 接口及请求、响应契约。
- `watermelon-auth`：SSO 授权流程及认证领域能力。
- `watermelon-service`：用户、角色、资源和资源关系等业务实现与服务入口。

对应前端位于 `../../js/watermelon-vue`。修改 HTTP 接口、字段或权限码时，同步检查前端类型和调用方。

## 项目约定

- 沿用现有 `api`、`application`、`domain`、`infrastructure`、`rpc` 分层和 converter/repository 边界。
- 跨服务契约放在 `watermelon-api`；Web 专用 DTO/VO 留在服务模块。
- 认证、权限和令牌逻辑不得绕过现有拦截器、上下文、repository 或权限码体系。
- 不提交密钥、口令、令牌、Cookie、真实连接串、私有服务地址或个人信息；配置示例只使用明显占位符。
- 若同时修改了前端，按其 `AGENTS.md` 执行前端验证。

## Git 提交

- 生成 Git commit message 时必须使用中文说明，不得使用纯英文；如采用 Conventional Commits，可保留 `feat:`、`fix:` 等类型前缀，但正文必须为中文。
