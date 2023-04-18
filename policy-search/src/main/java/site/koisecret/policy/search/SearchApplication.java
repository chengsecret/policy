package site.koisecret.policy.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @author by chengsecret
 * @date 2023/3/28.
 */
@EnableDiscoveryClient  //nacos
@SpringBootApplication
@EnableCircuitBreaker //开启Hystrix
@EnableHystrixDashboard // 激活仪表盘
@MapperScan("site.koisecret.policy.search.mapper")
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
