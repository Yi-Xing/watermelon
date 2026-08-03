# 项目与依赖关系

此文件记录当前可复用的项目拓扑。执行命令前重新核对相关 `pom.xml`，因为模块和依赖可能继续演进。

## 仓库职责

| 仓库 | Maven 模块 | 主要职责 |
| --- | --- | --- |
| `water` | `water-common`、`water-log`、`water-framework`、`water-dubbo`、`water-auth` | Java 21 / Spring Boot 3.2 的共享基础组件、日志、框架、Dubbo 和认证能力 |
| `watermelon` | `watermelon-api`、`watermelon-auth`、`watermelon-service` | RBAC 用户权限系统；API 契约、认证扩展和 DDD 服务实现 |
| `banana` | `banana-api`、`banana-service` | 文件管理系统；API 契约和 DDD 服务实现 |

## 跨仓库依赖方向

```text
water
  └─> watermelon
        └─> banana（通过 watermelon-api）
  └────────────────> banana
```

- `watermelon-api` 使用 `water-common`。
- `watermelon-auth` 使用 `watermelon-api` 和 `water-auth`。
- `watermelon-service` 使用 `watermelon-api`、`watermelon-auth` 和 `water-dubbo`。
- `banana` 的父 POM 是本地 `top.fblue:water:0.0.1-SNAPSHOT`，且 `relativePath` 为空，因此构建前需要本地仓库中已有对应的 `water` 父 POM。
- `banana-service` 使用 `water-dubbo`、`water-auth` 和 `watermelon-api`。
- 三个仓库当前共享版本 `0.0.1-SNAPSHOT`；不要假设此版本永远不变，以当前 POM 为准。

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
