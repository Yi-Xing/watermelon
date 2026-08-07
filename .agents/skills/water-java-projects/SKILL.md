---
name: water-java-projects
description: 开发、诊断、重构、审查、构建和测试同一父目录下的 ../water、../watermelon、../banana 三个 Java 21 Maven 多模块项目。Codex 在其中任一仓库处理 Java 代码、配置、依赖、测试、模块边界或跨仓库改动时使用；遵守共享基础库与业务项目的边界，并按依赖顺序验证受影响模块。
---

# Water Java Projects

按本工作流处理 `water`、`watermelon` 和 `banana`。先确认事实，再做最小范围修改，并验证直接影响和必要的下游影响。

## 开始工作

1. 确认当前仓库、目标模块和用户要求的改动范围。
2. 运行 `git status --short`，保留用户已有改动，不覆盖或清理无关文件。
3. 阅读根 `pom.xml`、受影响模块的 `pom.xml`、相邻实现和现有测试；优先用 `rg` 查找符号和调用方。
4. 涉及模块归属、Maven 依赖、公共契约或跨仓库验证时，读取 [references/project-map.md](references/project-map.md)。若文件内容与当前 POM 不一致，以当前 POM 为准，并在完成后修正该引用。

## 维护架构边界

- 将通用基础能力放在 `water`，避免把 `banana` 或 `watermelon` 的业务语义引入共享模块。
- 在 `watermelon` 和 `banana` 中沿用现有 `api`、`application`、`domain`、`infrastructure`、`rpc` 分层；新增代码前先寻找同类实现并保持包结构、命名和转换方式一致。
- 将跨服务公开类型放入对应 `*-api` 模块；不要让 API 模块依赖服务实现模块。
- 保持领域接口与基础设施实现分离。不要为了局部需求绕过现有 repository、converter 或 service 边界。
- 只有在调用方确实需要时才扩大公共 API；公共契约变化必须搜索三个仓库中的所有引用。

### 强制后端调用链

- `watermelon` 和 `banana` 的业务代码必须遵循 `API/Controller 或 RPC -> Application Service -> Domain Service -> Repository 端口 -> Repository 实现 -> Mapper`。每一层只能沿该方向调用下一层，不得跳层调用或反向依赖。
- API、Controller 和 RPC 实现只负责协议适配、入参校验及调用 Application Service，不得直接调用 Domain Service、Repository 或 Mapper。
- Application Service 负责用例编排，只能调用 Domain Service；不得直接调用 Repository 或 Mapper。Domain Service 承载业务规则并调用 Repository 端口，不得直接调用 Mapper。Repository 实现负责持久化适配，只能调用 Mapper、Converter 或外部基础设施客户端，不得调用其他 Repository、Domain Service 或 Application Service。
- 禁止业务组件同层互调，包括但不限于 `Application Service -> Application Service`、`Domain Service -> Domain Service`、`Repository -> Repository`、`API/RPC -> API/RPC`。需要组合能力时，应将编排上移到上一层，或将公共规则下沉到下一层的合适抽象。
- Converter、Validator、Factory 等无状态辅助组件不视为同层业务编排，但只能承担转换、校验或创建职责，不得在其中访问数据库、发起 RPC 或组织业务流程。
- 分层例外必须是少数且有明确技术原因的场景，不能仅以“调用方便”作为理由。出现例外时，必须在异常依赖或调用位置使用“分层例外”注释说明原因、适用范围和未采用标准调用链的理由；长期保留的例外还必须记录到对应仓库的 `AGENTS.md` 或设计文档，并在交付说明中明确指出。未标注的现有违规实现不得作为新代码范例。

### Application Service 代码风格

- Application Service 方法必须保持自上而下、步骤清晰的线性编排。两个及以上步骤使用 `// 1. ...`、`// 2. ...` 的编号注释说明意图；每条步骤注释后必须有对应代码，不得出现只有编号注释而没有实现的空步骤。
- 请求判空、字段格式、组合参数和规范化处理统一封装到专用 Validator，不得在 Application Service 方法中堆叠 `request == null`、`StringUtils.hasText(...)` 或大量私有校验方法。
- DTO、Domain、PO、VO、RPC Request/Response 之间的组装统一封装到 Converter；Application Service 不得手写大段 `builder()` 或逐字段复制。Application Service 只接收 Validator 的校验结果、调用 Domain Service、处理必要的结果分支，并调用 Converter 返回结果。
- 业务规则和状态变更必须由 Domain Service 完成。Application Service 可以进行“查询结果不存在则抛出用例异常”这类必要分支，但不得把可复用业务规则写回编排方法。
- 业务变量使用明确类型，不使用 `var`；方法名、变量名、步骤注释必须表达业务含义，避免 `handle`、`processData`、`result1` 等模糊命名。
- 公共业务方法必须有与真实签名和行为一致的 Javadoc，或在实现方法上使用准确的 `{@inheritDoc}`。修改参数、返回值或职责时必须同步修正文档，不得保留不存在的 `@param`，也不得用注释描述实际没有执行的逻辑。
- 推荐的 Application Service 方法结构如下；新增和重构代码应优先保持这种形态：

```java
@Override
public Response execute(Request request) {
    // 1. 校验并规范化请求参数
    ValidatedInput input = requestValidator.validate(request);

    // 2. 调用领域服务执行业务能力
    DomainResult domainResult = domainService.execute(input);

    // 3. 处理当前用例必要的结果分支
    if (domainResult == null) {
        throw new BusinessException("目标数据不存在");
    }

    // 4. 通过转换器构建响应
    return responseConverter.toResponse(domainResult);
}
```

- 禁止把上述结构写成“方法内联参数校验 + Application 私有校验方法 + Domain 调用 + 手写 Response Builder”的大方法。发现旧代码采用这种写法时，不得照抄；本次任务涉及该调用链时应按 Validator、Domain Service、Converter 的职责拆分。

## 实施改动

1. 先定位根因或现有扩展点，再编辑文件。
2. 控制改动范围，不顺带重构无关代码，不改变未被请求的外部行为。
3. 为行为变化补充或更新最接近受影响代码的测试；沿用项目现有的 JUnit、Spring 测试和断言风格。
4. 不把密钥、数据库口令、JWT secret 或本机环境配置写入仓库。
5. 遇到本地缺少 `top.fblue` SNAPSHOT 依赖时，按项目依赖顺序安装上游模块，不通过随意改版本或删除依赖绕过问题。

## 验证改动

- 执行本地启动、HTTP、浏览器、SSO、权限或 Dubbo 跨系统联调前，先读取 [references/project-map.md](references/project-map.md) 的本地域名约定并验证 hosts 解析；测试必须使用约定域名，不得以 `localhost` 或 `127.0.0.1` 替代。
- 优先在仓库根目录运行最小充分验证：`mvn -pl <module> -am test`。
- 修改父 POM、共享模块或多个模块时，运行该仓库的 `mvn test`。
- 修改 `water` 的公开类型或行为时，先验证并安装 `water`，再验证受影响的 `watermelon` 或 `banana` 模块。
- 修改 `watermelon-api` 时，验证并安装该模块及其上游，再验证 `banana-service`。
- 仅当改动跨越公共依赖边界或用户明确要求时，运行三个仓库的完整构建链。
- 若测试需要 MySQL、Dubbo、外部服务或环境变量而当前环境未提供，先完成可运行的单元/编译验证，并明确报告未运行项和缺失条件。

## 交付结果

简要说明修改内容、影响模块、已运行命令及结果。若存在未验证项、下游兼容风险或所需环境，明确列出，不宣称未经执行的检查已通过。
