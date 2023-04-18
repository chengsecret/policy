package site.koisecret.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author by chengsecret
 * @date 2023/3/29.
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("site.koisecret.auth.mapper")
@EnableFeignClients //添加对Feign的支持
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
