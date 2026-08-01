package top.fblue.watermelon;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Watermelon 用户中心服务启动入口。
 */
@SpringBootApplication(scanBasePackages = {"top.fblue.watermelon", "top.fblue.dubbo"})
@EnableDubbo
public class WatermelonServiceApplication {

	/**
	 * 启动 Spring Boot 用户中心服务。
	 *
	 * @param args 命令行启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(WatermelonServiceApplication.class, args);
	}

}
