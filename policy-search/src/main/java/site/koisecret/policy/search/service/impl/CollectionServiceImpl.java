package site.koisecret.policy.search.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.commons.utils.JWTUtils;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.mapper.CollectionMapper;
import site.koisecret.policy.search.service.CollectionService;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/4.
 */
@Service
@Slf4j
public class CollectionServiceImpl implements CollectionService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CollectionMapper collectionMapper;


    @Override
    public Result collectionList(String token, Result result) {

        try {
            Claims claims = JWTUtils.parseJWT(token);
            String uid = claims.get("uid").toString();
            // 是否收藏
            List collections = (List<String>) redisTemplate.opsForValue().get(RedisKey.USER_COLLECTION + uid);
            if (null == collections) {
                collections = collectionMapper.findPidsByUid(Integer.parseInt(uid));
                redisTemplate.opsForValue().set(RedisKey.USER_COLLECTION + uid, collections, Duration.ofHours(1));
            }
            JSONArray records = (JSONArray) JSONUtil.parseObj(result.getData().get("page")).get("records");
            int[] ints = new int[records.size()];
            Arrays.fill(ints, 0); //0表示未收藏
            for (int i = 0; i < records.size(); i++) {
                Policy policy = JSONUtil.toBean(records.get(i).toString(), Policy.class);
                if(collections.contains( policy.getPolicyId()  ))
                    ints[i]=1;  //1表示收藏
            }
            return result.set("collection",ints);
        } catch (ExpiredJwtException expiredJwtEx) {
            log.info("token : {} 过期", token );
            result.setMessage("token过期");
            return result.set("collection","");
        } catch (Exception ex) {
            log.info("token : {} 验证失败" , token );
            result.setMessage("token验证失败");
            return result.set("collection","");
        }

    }

    @Override
    public Result collectionListHaveAuth(String uid, Result result) {
        // 是否收藏
        List collections = (List<String>) redisTemplate.opsForValue().get(RedisKey.USER_COLLECTION + uid);
        if (null == collections) {
            collections = collectionMapper.findPidsByUid(Integer.parseInt(uid));
            redisTemplate.opsForValue().set(RedisKey.USER_COLLECTION + uid, collections, Duration.ofHours(1));
        }
        JSONArray records = (JSONArray)JSONUtil.parseObj(result.getData().get("page")).get("records");
        int[] ints = new int[records.size()];
        Arrays.fill(ints, 0); //0表示未收藏
        for (int i = 0; i < records.size(); i++) {
            Policy policy = JSONUtil.toBean(records.get(i).toString(), Policy.class);
            if(collections.contains( policy.getPolicyId()  ))
                ints[i]=1;  //1表示收藏
        }

        return result.set("collection",ints);
    }
}
