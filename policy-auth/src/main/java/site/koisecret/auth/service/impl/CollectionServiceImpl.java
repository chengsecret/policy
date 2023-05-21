package site.koisecret.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.koisecret.auth.entity.Behavior;
import site.koisecret.auth.entity.Collection;
import site.koisecret.auth.entity.PolicySQL;
import site.koisecret.auth.mapper.BehaviorMapper;
import site.koisecret.auth.mapper.CollectionMapper;
import site.koisecret.auth.mapper.PolicySQLMapper;
import site.koisecret.auth.service.CollectionService;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;

import java.time.Duration;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@Service
@Slf4j
@Transactional
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private BehaviorMapper behaviorMapper;
    @Autowired
    private PolicySQLMapper policySQLMapper;

    @Override
    public Result addCollection(Collection collection) {

        if(null != collectionMapper.findCollection(collection.getUid(), collection.getPid())){
            return Result.FAIL("您已收藏该政策信息");
        }

        if (collectionMapper.addCollection(collection)) {

            PolicySQL policySQL = policySQLMapper.findPolicySQLByPid(collection.getPid());
            if (policySQL != null) {
                Integer collectionTimes = policySQL.getColectionTimes();
                if (null == collectionTimes) {
                    policySQLMapper.updateCollectionTimes(1, collection.getPid());
                }else {
                    policySQLMapper.updateCollectionTimes(collectionTimes + 1, collection.getPid());
                }
            }


            if(Boolean.TRUE.equals(redisTemplate.hasKey(RedisKey.USER_COLLECTION + collection.getUid()))){
                redisTemplate.delete(RedisKey.USER_COLLECTION + collection.getUid());
            }

            //修改浏览记录表中的 bid
            Behavior behavior = behaviorMapper.findBehavior(collection.getUid(), collection.getPid());
            if (null == behavior) {
                behavior = new Behavior(-1, collection.getUid(), collection.getPid(), 2, 1, null);
                if (behaviorMapper.addBehavior(behavior)) {
                    if(Boolean.TRUE.equals(redisTemplate.hasKey(RedisKey.USER_BEHAVIOR_PID + collection.getUid()))){
                        redisTemplate.delete(RedisKey.USER_BEHAVIOR_PID + collection.getUid());
                    }
                    return Result.SUCCESS();
                } else {
                    return Result.FAIL("收藏失败");
                }
            }else {
                behavior.setBid(2);
                behaviorMapper.updateBehavior(behavior);
                return Result.SUCCESS();
            }
        }
        return Result.FAIL("收藏失败");
    }

    @Override
    public Result delCollection(Collection collection) {
        if(null == collectionMapper.findCollection(collection.getUid(), collection.getPid())){
            return Result.FAIL("您未受藏该政策");
        }
        if (collectionMapper.delCollection(collection.getUid(), collection.getPid())) {
            if(Boolean.TRUE.equals(redisTemplate.hasKey(RedisKey.USER_COLLECTION + collection.getUid()))){
                redisTemplate.delete(RedisKey.USER_COLLECTION + collection.getUid());
            }

            //修改浏览记录表中的 bid
            if(behaviorMapper.update(3, collection.getUid(), collection.getPid())){
                return Result.SUCCESS();
            }else {
                return Result.FAIL("取消收藏失败");
            }
        }
        return Result.FAIL("取消失败");
    }

    @Override
    public List<String> findPidsByUid(int uid) {

        List<String> collections = (List<String>) redisTemplate.opsForValue().get(RedisKey.USER_COLLECTION + uid);
        if (null == collections) {
            collections = collectionMapper.findPidsByUid(uid);
            redisTemplate.opsForValue().set(RedisKey.USER_COLLECTION + uid, collections, Duration.ofHours(1));
        }

        return collections;
    }
}
