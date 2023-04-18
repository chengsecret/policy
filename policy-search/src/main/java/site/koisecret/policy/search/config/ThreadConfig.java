package site.koisecret.policy.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author by chengsecret
 * @date 2023/4/9.
 */
@Configuration
public class ThreadConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties threadPoolConfigProperties) {
        return new ThreadPoolExecutor(
                threadPoolConfigProperties.getCorePoolSize(),
                threadPoolConfigProperties.getMaximumPoolSize(),
                threadPoolConfigProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(threadPoolConfigProperties.getDequeCapacity()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

}
