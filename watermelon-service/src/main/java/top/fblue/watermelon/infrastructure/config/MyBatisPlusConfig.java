package top.fblue.watermelon.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.fblue.watermelon.infrastructure.interceptor.SqlLogInterceptor;

/**
 * MyBatis Plus 配置类
 * 注册 SQL 执行拦截器
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("自定义 MyBatis Plus 拦截器已注册");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 乐观锁插件
//        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
    /**
     * 注册 日志SQL执行拦截器
     */
    @Bean
    public SqlLogInterceptor sqlExecutionInterceptor() {
        log.info("自定义 SQL 日志拦截器已注册");
        return new SqlLogInterceptor();
    }
}
