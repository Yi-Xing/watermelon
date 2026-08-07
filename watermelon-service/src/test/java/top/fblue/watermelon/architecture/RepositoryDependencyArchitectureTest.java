package top.fblue.watermelon.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository 基础设施适配器依赖边界测试。
 */
class RepositoryDependencyArchitectureTest {

    private static final Pattern REPOSITORY_ADAPTER_PATTERN = Pattern.compile(
            ".*\\.infrastructure\\.repository\\..*Repository(?:Impl)?");

    /**
     * Repository 适配器只能依赖 Mapper、Converter 或其他底层客户端，
     * 不允许通过字段、构造器或方法参数依赖另一个 Repository。
     *
     * @throws ClassNotFoundException 扫描到的类无法加载时抛出
     */
    @Test
    void repositoryAdaptersShouldNotDependOnOtherRepositories() throws ClassNotFoundException {
        Set<String> scannedAdapters = new TreeSet<>();
        Set<String> violations = findRepositoryDependencies("top.fblue.watermelon", scannedAdapters);

        assertFalse(scannedAdapters.isEmpty(), "未扫描到 Repository 适配器，架构测试配置可能失效");
        assertTrue(violations.isEmpty(),
                () -> "Repository 之间不允许相互依赖：\n" + String.join("\n", violations));
    }

    /**
     * 扫描指定基础包中的 Repository 适配器依赖。
     *
     * @param basePackage 扫描基础包
     * @param scannedAdapters 已扫描的 Repository 适配器类名
     * @return Repository 依赖违规信息
     * @throws ClassNotFoundException 扫描到的类无法加载时抛出
     */
    private Set<String> findRepositoryDependencies(String basePackage,
                                                   Set<String> scannedAdapters)
            throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(REPOSITORY_ADAPTER_PATTERN));

        Set<String> violations = new TreeSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            Class<?> adapterClass = Class.forName(candidate.getBeanClassName());
            scannedAdapters.add(adapterClass.getName());
            collectFieldDependencies(adapterClass, violations);
            collectConstructorDependencies(adapterClass, violations);
            collectMethodDependencies(adapterClass, violations);
        }
        return violations;
    }

    /**
     * 收集字段声明中的 Repository 依赖。
     *
     * @param adapterClass Repository 适配器类型
     * @param violations 违规信息集合
     */
    private void collectFieldDependencies(Class<?> adapterClass, Set<String> violations) {
        for (Field field : adapterClass.getDeclaredFields()) {
            addViolationIfRepository(
                    adapterClass, "字段 " + field.getName(), field.getType(), violations);
        }
    }

    /**
     * 收集构造器参数中的 Repository 依赖。
     *
     * @param adapterClass Repository 适配器类型
     * @param violations 违规信息集合
     */
    private void collectConstructorDependencies(Class<?> adapterClass, Set<String> violations) {
        for (Constructor<?> constructor : adapterClass.getDeclaredConstructors()) {
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                addViolationIfRepository(adapterClass, "构造器参数", parameterType, violations);
            }
        }
    }

    /**
     * 收集方法参数中的 Repository 依赖。
     *
     * @param adapterClass Repository 适配器类型
     * @param violations 违规信息集合
     */
    private void collectMethodDependencies(Class<?> adapterClass, Set<String> violations) {
        for (Method method : adapterClass.getDeclaredMethods()) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                addViolationIfRepository(
                        adapterClass, "方法 " + method.getName() + " 参数", parameterType, violations);
            }
        }
    }

    /**
     * 在依赖类型为 Repository 时记录违规信息。
     *
     * @param adapterClass Repository 适配器类型
     * @param dependencyLocation 依赖位置
     * @param dependencyType 依赖类型
     * @param violations 违规信息集合
     */
    private void addViolationIfRepository(Class<?> adapterClass,
                                          String dependencyLocation,
                                          Class<?> dependencyType,
                                          Set<String> violations) {
        if (isRepositoryType(dependencyType)) {
            violations.add(adapterClass.getName() + " 的 " + dependencyLocation
                    + " 依赖了 " + dependencyType.getName());
        }
    }

    /**
     * 判断类型是否为 Repository。
     *
     * @param type 待判断类型
     * @return 类型名称以 Repository 或 RepositoryImpl 结尾时返回 {@code true}
     */
    private boolean isRepositoryType(Class<?> type) {
        return type.getSimpleName().endsWith("Repository")
                || type.getSimpleName().endsWith("RepositoryImpl");
    }
}
