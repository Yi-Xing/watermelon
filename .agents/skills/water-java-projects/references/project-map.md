# 项目与依赖关系

此文件记录当前可复用的项目拓扑。执行命令前重新核对相关 `pom.xml`，因为模块和依赖可能继续演进。

## 仓库职责

| 仓库 | Maven 模块 | 主要职责 |
| --- | --- | --- |
| `water` | `water-common`、`water-log`、`water-framework`、`water-dubbo`、`water-auth` | Java 21 / Spring Boot 3.2 的共享基础组件、日志、框架、Dubbo 和认证能力 |
| `watermelon` | `watermelon-api`、`watermelon-auth`、`watermelon-service` | RBAC 用户权限系统；API 契约、认证与运行时鉴权、权限数据管理和 DDD 服务实现 |
| `banana` | `banana-api`、`banana-service` | 文件管理系统；API 契约和 DDD 服务实现 |

## 跨仓库依赖方向

```text
water
  └─> watermelon
        └─> banana（通过 watermelon-api）
  └────────────────> banana
```

- `watermelon-api` 使用 `water-common`。
- `watermelon-auth` 使用 `watermelon-api` 和 `water-auth`，定义认证、运行时鉴权逻辑及权限数据查询端口，不得反向依赖 `watermelon-service`。
- `watermelon-service` 使用 `watermelon-api`、`watermelon-auth` 和 `water-dubbo`，负责权限数据管理并实现 auth 模块所需的查询端口。
- Watermelon 权限变更通过 `watermelon-auth` 的 MySQL 事务发件箱记录，调用链保持 `Application -> Domain Service -> Repository`；后台任务领取记录后更新 Redis 权限版本并通知接入系统。
- `banana` 的父 POM 是本地 `top.fblue:water:0.0.1-SNAPSHOT`，且 `relativePath` 为空，因此构建前需要本地仓库中已有对应的 `water` 父 POM。
- `banana-service` 使用 `water-dubbo`、`water-auth` 和 `watermelon-api`。
- 三个仓库当前共享版本 `0.0.1-SNAPSHOT`；不要假设此版本永远不变，以当前 POM 为准。

## 本地跨系统联调地址

| 系统 | 前端 | HTTP 后端 | Dubbo |
| --- | --- | --- | --- |
| Watermelon | `http://watermelon.fblue.top:3000` | `http://watermelon.fblue.top:8080` | `watermelon.fblue.top:20880` |
| Banana | `http://banana.fblue.top:5173` | `http://banana.fblue.top:8081` | `banana.fblue.top:20881` |

本机 `/etc/hosts` 必须包含：

```text
127.0.0.1 watermelon.fblue.top
127.0.0.1 banana.fblue.top
```

- 启动验证、HTTP 探测、浏览器操作、SSO 回调、权限动态刷新和 Dubbo 直连测试均使用上述域名，不能混用 `localhost` 或 `127.0.0.1`。
- 测试前先确认两个域名均解析到 `127.0.0.1`；缺少映射时先请求用户授权修改系统 hosts，不得静默回退到其他地址。
- 使用 `curl` 测试时若本机配置了 HTTP 代理，应通过 `--noproxy '*'` 保证请求直连本机。

## 常用验证命令

在对应仓库根目录执行：

```bash
mvn -pl <module> -am test
mvn test
```

需要把上游 SNAPSHOT 提供给下游仓库时：

```bash
mvn -f ../water/pom.xml install
mvn -f ../watermelon/pom.xml -pl watermelon-api -am install
mvn -f ../banana/pom.xml -pl banana-service -am test
```

根据实际影响裁剪构建链：只改 `watermelon` 内部实现且未改变 API 时，不必验证 `banana`；只改 `banana` 时，不必重建无变化的上游，除非本地依赖尚未安装。
