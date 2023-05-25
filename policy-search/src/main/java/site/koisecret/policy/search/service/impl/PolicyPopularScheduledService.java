package site.koisecret.policy.search.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.policy.search.dto.PopularPolicyDTO;
import site.koisecret.policy.search.mapper.PolicySQLMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/5/21.
 */
@Service
public class PolicyPopularScheduledService {
    @Autowired
    private PolicySQLMapper policySQLMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updatePopularPolicy() {
        sortPopularPolicy();
    }

    public List<PopularPolicyDTO> sortPopularPolicy() {
        List<PopularPolicyDTO> popularPolicy = policySQLMapper.get();

        // 使用自定义比较器进行排序
        popularPolicy.sort((policy1, policy2) -> {
            int sum1 = policy1.getColectionTimes() + policy1.getBrowseTimes();
            int sum2 = policy2.getColectionTimes() + policy2.getBrowseTimes();
            // 从大到小排序
            return Integer.compare(sum2, sum1);
        });

        ArrayList<PopularPolicyDTO> popularPolicyDTOS = new ArrayList<>(popularPolicy.subList(0, 1000));
        redisTemplate.opsForValue().set(RedisKey.POLICY_POPULAR, popularPolicyDTOS);
        return popularPolicyDTOS;
    }

}
