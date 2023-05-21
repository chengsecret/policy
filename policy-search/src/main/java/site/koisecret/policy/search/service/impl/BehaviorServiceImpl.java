package site.koisecret.policy.search.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.policy.search.entity.Behavior;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.entity.PolicySQL;
import site.koisecret.policy.search.es.ESClient;
import site.koisecret.policy.search.es.ESIndexConstants;
import site.koisecret.policy.search.mapper.BehaviorMapper;
import site.koisecret.policy.search.mapper.PolicySQLMapper;
import site.koisecret.policy.search.service.BehaviorService;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/6.
 */
@Service
public class BehaviorServiceImpl implements BehaviorService {
    @Autowired
    private BehaviorMapper behaviorMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ESClient client;

    @Autowired
    private PolicySQLMapper policySQLMapper;

    @Override
    public Result browseRecord(int uid, Page<Policy> page) throws IOException {
        List<String> pids = (List<String>) redisTemplate.opsForValue().get(RedisKey.USER_BEHAVIOR_PID + uid);
        if (null == pids) {
            pids = behaviorMapper.findPidsByUid(uid);
            redisTemplate.opsForValue().set(RedisKey.USER_BEHAVIOR_PID + uid, pids, Duration.ofHours(1));
        }
        String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
        return client.getPoliciesByIds(String.valueOf(uid), page, pids, index, Policy.class);
    }

    @Override
    public Result addBehavior(int uid, String pid) {
//        PolicySQL policySQL = policySQLMapper.findPolicySQLByPid(pid);
//        if (policySQL != null) {
//            Integer browseTimes = policySQL.getBrowseTimes();
//            if (null == browseTimes) {
//                policySQLMapper.updateBrowseTimes(1, pid);
//            }else {
//                policySQLMapper.updateBrowseTimes(browseTimes + 1, pid);
//            }
//        }

        Behavior behavior = behaviorMapper.findBehavior(uid, pid);
        if (null == behavior) {
            behavior = new Behavior(-1, uid, pid, 1, 1, null);
            if (behaviorMapper.addBehavior(behavior)) {
                policySQLMapper.updateBrowseTimes(1, pid);
                if(Boolean.TRUE.equals(redisTemplate.hasKey(RedisKey.USER_BEHAVIOR_PID + uid))){
                    redisTemplate.delete(RedisKey.USER_BEHAVIOR_PID + uid);
                }
                return Result.SUCCESS();
            }else {
                return Result.FAIL("添加浏览记录失败");
            }
        }else {
            behavior.setTimes(behavior.getTimes() + 1);
            if (behaviorMapper.updateBehavior(behavior)) {
                PolicySQL policySQL = policySQLMapper.findPolicySQLByPid(pid);
                policySQLMapper.updateBrowseTimes(policySQL.getBrowseTimes() + 1, pid);
                return Result.SUCCESS();
            }else {
                return  Result.FAIL("更新浏览记录失败");
            }
        }
    }
}
