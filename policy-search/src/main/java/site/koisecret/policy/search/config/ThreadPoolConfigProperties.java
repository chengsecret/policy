package site.koisecret.policy.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author by chengsecret
 * @date 2023/4/9.
 */
@Component
@ConfigurationProperties(prefix = "thread")
@Data
public class ThreadPoolConfigProperties {
    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private int dequeCapacity;

}
