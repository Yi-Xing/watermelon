# SSO 改造文件删除说明

## 1. 说明范围

本文记录本次 SSO 2.1（不含第 9 条可靠重试队列）、2.2、2.3 实现过程中，在 `watermelon` 项目中删除的文件、删除原因和替代实现。

这些删除不是简单裁掉功能，主要目的是把可复用的认证、JWT、用户上下文和 Dubbo Filter 迁移到公共依赖项目 `water/water-auth`，并消除 `watermelon-auth`、`watermelon-service` 中的重复实现。需要成套提交三个项目的改动，不能只提交删除记录而遗漏替代文件。

截至本文生成时：

- `water` 项目没有删除文件；
- `banana` 项目没有删除文件；
- `watermelon` 项目共有 20 个 Git 删除项，逐项说明如下。

## 2. 删除文件明细

| 删除文件 | 原职责 | 删除原因 | 替代实现及影响 |
|---|---|---|---|
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/api/AuthCallbackController.java` | 旧版认证回调入口 | 旧入口未实现本次设计要求的 Client 注册校验、redirectUri 精确白名单、state 透传和一次性授权码流程，继续保留会形成两套 SSO 协议入口 | 由 `SsoAuthorizationController` 提供 `/api/sso/authorize`；Client 侧的 start/callback/exchange 流程由 `banana` 的 `SsoController` 承担。旧实现功能已按新协议替换 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/application/service/UserAuthApplicationService.java` | 旧认证应用服务接口 | 名称与 `watermelon-service` 中本地账号登录服务 `UserAuthApplicationService` 完全相同，迫使调用方使用全限定类名，也无法准确表达“SSO 授权服务”的边界 | 重命名并重构为 `SsoAuthorizationApplicationService`。这是职责澄清和消除类名冲突，不是删除业务能力 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/application/service/impl/UserAuthApplicationServiceImpl.java` | 旧认证应用服务实现 | 与本地登录实现同名，且旧实现不完整；保留会导致可读性差、Bean/类型识别困难 | 重命名并重构为 `SsoAuthorizationApplicationServiceImpl`，承接会话创建、授权回调、code 兑换和全局退出 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/annotation/RpcPublic.java` | 标记无需用户上下文的公共 RPC | 注解属于所有业务项目都会使用的 RPC 协议能力，不应由用户中心业务模块私有定义 | 使用 `water-common` 中的 `top.fblue.common.annotation.RpcPublic`，避免不同项目出现同名但类型不相等的注解 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/constant/UserConst.java` | 保存旧 Token、请求属性或 RPC attachment 常量 | 常量与认证协议强绑定，并且会被多个系统使用；留在用户中心会造成复制和取值不一致 | 统一迁移到 `water-auth` 的 `SsoConstants`，每个常量均补充了用途注释 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/context/UserContext.java` | 保存 HTTP 请求线程中的当前用户 | 与公共 SSO 拦截器必须共享同一个 `ThreadLocal`；保留业务模块副本会出现“拦截器已写入、业务代码却读取不到”的问题 | 使用 `water-auth` 的 `UserContext`，并由 `SsoAuthenticationInterceptor` 在请求结束及异步切换时统一清理 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/context/UserRpcContext.java` | 保存 Dubbo Provider 调用线程中的用户 | 与公共 Dubbo Filter 必须共享同一个上下文实例，业务模块副本会导致上下文割裂 | 使用 `water-auth` 的 `UserRpcContext`，由公共 Provider Filter 绑定和清理 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/dto/UserDTO.java` | 旧版跨认证链路用户 DTO | 字段不足以表达 sid、jti、issuer、audience、clientId 和 Token 时间范围，不能满足 SSO 校验及跨 RPC 透传 | 使用 `water-auth` 的 `SsoPrincipal` 作为认证主体模型；业务用户实体仍保留在各自领域层 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/utils/RpcPublicUtils.java` | 判断 Dubbo 方法是否标注 `RpcPublic` | 该判断逻辑只服务于公共 Dubbo Filter，不应留在用户中心业务模块 | 迁移到 `water-auth` 的 `top.fblue.auth.dubbo.filter.RpcPublicUtils`，与 Filter 同包并限制为包级可见 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/common/utils/TokenUtil.java` | 旧 JWT 生成或解析工具 | 旧工具缺少 issuer、audience、sid、jti、算法固定、时钟偏差和统一异常等完整校验，且静态工具难以按 Client 配置 | 由 `water-auth` 的 `JwtTokenService` 负责签发和严格校验，由 `BearerTokenResolver` 负责解析 HTTP Bearer Header |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/config/JwtAuthConfig.java` | 旧 JWT 配置 | 配置仅属于用户中心，无法复用于 banana，并且不能通过公共自动配置装配拦截器和 Redis 撤销仓储 | 使用 `water-auth` 的 `SsoAuthProperties` 与 `SsoWebMvcAutoConfiguration`；各应用在自身 `application-dev/prod.properties` 中提供 Client 级配置 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/converter/AuthRpcConverter.java` | 旧认证 RPC 对象转换 | 新的 code 兑换响应需要 sid、sessionExpireAt 等字段，旧转换模型已不匹配；为单一简单对象保留转换类会增加无效跳转 | 由 `SsoAuthorizationApplicationServiceImpl` 在完成会话、用户及授权码校验后显式构造 `CodeExchangeResponse` |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/filter/DubboUserConsumerFilter.java` | 用户中心私有的 Dubbo Consumer 用户透传 | banana 等系统同样需要该能力；各项目复制 Filter 会造成 attachment key、清理时机和启用顺序不一致 | 迁移到 `water-auth` 的 `DubboUserConsumerFilter`，通过公共 Dubbo SPI 自动启用 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/filter/DubboUserProviderFilter.java` | 用户中心私有的 Dubbo Provider 用户上下文恢复 | 属于跨项目公共能力，旧实现也没有覆盖完整 SSO principal 字段 | 迁移到 `water-auth` 的 `DubboUserProviderFilter`，增加必要 attachment 完整性和 userId 格式校验 |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/handler/AutoFillMetaObjectHandler.java` | MyBatis 自动填充时读取当前用户 | `watermelon-service` 已有实际生效的自动填充 Handler；认证模块再声明一个会造成重复 Bean/重复填充风险 | 保留并调整 `watermelon-service` 的 `AutoFillMetaObjectHandler`，改为读取公共 `UserContext` |
| `watermelon-auth/src/main/java/top/fblue/watermelon/auth/infrastructure/interceptor/TokenAuthInterceptor.java` | 用户中心认证模块中的旧 HTTP Token 拦截器 | 与公共 SSO 拦截器重复，继续注册可能对同一请求解析两次 Token、使用不同上下文并产生冲突响应 | 使用 `water-auth` 自动配置创建的 `SsoAuthenticationInterceptor`，统一处理 Bearer Token、JWT claims 和 sid/jti 撤销状态 |
| `watermelon-auth/src/main/resources/META-INF/dubbo/org.apache.dubbo.rpc.Filter` | 注册用户中心私有 Dubbo 用户 Filter | Filter 类已迁移，原 SPI 文件继续存在会引用不存在的类或造成两套 Filter 重复加载 | 使用 `water-auth/src/main/resources/META-INF/dubbo/org.apache.dubbo.rpc.Filter` 统一注册公共 Consumer/Provider Filter |
| `watermelon-auth/src/main/resources/application.properties` | 在库模块中保存旧认证配置 | `watermelon-auth` 是被 `watermelon-service` 引用的库模块，不应自行携带环境配置；库内配置会与最终应用的 dev/prod 配置发生覆盖或来源不明 | SSO 和用户中心 Server 配置已迁移到 `watermelon-service/src/main/resources/application-dev.properties` 与 `application-prod.properties` |
| `watermelon-service/src/main/java/top/fblue/watermelon/common/context/UserContext.java` | service 模块自己的 HTTP 用户上下文 | 与 `watermelon-auth` 旧上下文及公共上下文重复；多个 `ThreadLocal` 会导致登录用户在不同层不可见 | 全部调用统一改为 `water-auth` 的 `UserContext` |
| `watermelon-service/src/main/java/top/fblue/watermelon/infrastructure/interceptor/TokenAuthInterceptor.java` | service 模块自己的旧 Token 拦截器 | 与 `watermelon-auth` 旧拦截器、公共 SSO 拦截器形成第三套认证链路，存在重复解析、异常结构不一致和上下文不一致问题 | 由公共 `SsoAuthenticationInterceptor` 替代；用户中心自身的细粒度权限继续由 `PermissionAuthInterceptor` 处理 |

## 3. 为什么采用迁移和替换

本次结构调整后的职责边界为：

- `water/water-auth`：JWT、HTTP 认证拦截器、统一 SSO 异常响应、sid/jti 撤销仓储、HTTP/RPC 用户上下文、Dubbo 用户透传；
- `watermelon/watermelon-auth`：用户中心特有的全局会话、授权码、Client/redirectUri 校验、code 兑换和 Back-Channel 退出编排；
- `banana/banana-service`：Client 侧 state Cookie、一次性 ticket、本地 Token 签发、回调和退出接入。

这种拆分保证同一 JVM 中只有一套认证主体和上下文，两个业务系统也会使用相同的 JWT 校验、错误模型和 Dubbo attachment 协议。

## 4. 功能影响和回退方式

- 删除项对应的认证能力均有替代实现，未故意删除现有账号登录、权限校验、自动填充等业务能力。
- 旧的回调协议被新的一次性 code + state + redirectUri 白名单协议替换，旧接口调用方需要同步切换，不能继续依赖 `AuthCallbackController`。
- 2.1 第 9 条 Back-Channel 可靠重试队列按要求暂未实现；当前通知失败只记录 error 日志，不在本次删除范围内伪造一个不可靠的队列实现。
- 所有删除仍处于 Git 工作区改动中，可通过版本历史恢复；但单独恢复旧上下文、旧拦截器或旧 Dubbo Filter 会重新引入重复认证链路，不建议与新实现同时启用。

可使用以下命令核对当前实际删除清单：

```bash
git diff --diff-filter=D --name-status
```
