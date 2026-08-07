# Watermelon 项目指令

本文件适用于仓库根目录及全部子目录。

## 工作流

- 处理本仓库的 Java 开发、诊断、重构、审查、构建或测试任务前，完整读取并遵循 `.agents/skills/water-java-projects/SKILL.md`。
- 跨仓库依赖、模块边界、Maven 验证顺序和通用交付要求以 `$water-java-projects` 为准；本文只补充 Watermelon 特有约定。

## 仓库职责

Watermelon 是用户与权限服务：

- `watermelon-api`：跨服务 Dubbo 接口及请求、响应契约。
- `watermelon-auth`：SSO 授权流程、认证领域能力及运行时鉴权能力。
- `watermelon-service`：用户、角色、资源和资源关系等权限数据管理、服务入口及 auth 查询端口适配。

对应前端位于 `../../js/watermelon-vue`。修改 HTTP 接口、字段或权限码时，同步检查前端类型和调用方。

## 项目约定

- 沿用现有 `api`、`application`、`domain`、`infrastructure`、`rpc` 分层和 converter/repository 边界。
- 后端调用严格遵循 `API/Controller 或 RPC -> Application Service -> Domain Service -> Repository -> Mapper`，禁止跳层、反向调用和同层业务组件互调；完整判定与例外标注要求见 `$water-java-projects` 的“强制后端调用链”。
- Application Service 必须按 `$water-java-projects` 的“Application Service 代码风格”进行线性分步编排，统一使用 Validator、Domain Service 和 Converter，不得内联堆叠校验或手写响应 Builder。
- 跨服务契约放在 `watermelon-api`；Web 专用 DTO/VO 留在服务模块。
- 认证、权限和令牌逻辑不得绕过现有拦截器、上下文、repository 或权限码体系。
- 权限变更遵循 `Application -> Domain Service -> Repository`，由 `watermelon-auth` 的 MySQL 事务发件箱可靠下发；不得重新引入仅依赖进程内 Spring 事件的权限失效链路。
- 已批准的分层例外仅包括资源 Excel 导入导出：`ResourceApplicationServiceImpl` 同层调用 `ResourceExcelService`，且 `ResourceExcelServiceImpl` 为了在一个事务中完成整表差异计算和批量增删改而直接调用资源 Repository。该例外必须在代码现场保留“分层例外”说明，只适用于 Excel 技术处理流程，不得推广到普通业务用例；新增业务规则时应优先下沉至 Domain Service。
- 不提交密钥、口令、令牌、Cookie、真实连接串、私有服务地址或个人信息；配置示例只使用明显占位符。
- 若同时修改了前端，按其 `AGENTS.md` 执行前端验证。

## Git 提交

- 生成 Git commit message 时必须使用中文说明，不得使用纯英文；如采用 Conventional Commits，可保留 `feat:`、`fix:` 等类型前缀，但正文必须为中文。
