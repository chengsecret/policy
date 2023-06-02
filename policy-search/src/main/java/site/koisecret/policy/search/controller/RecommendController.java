package site.koisecret.policy.search.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.commons.utils.JWTUtils;
import site.koisecret.policy.search.dto.PopularPolicyDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.es.ESClient;
import site.koisecret.policy.search.es.ESIndexConstants;
import site.koisecret.policy.search.mapper.BehaviorMapper;
import site.koisecret.policy.search.service.CollectionService;
import site.koisecret.policy.search.service.impl.PolicyPopularScheduledService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

/**
 * @author by chengsecret
 * @date 2023/5/21.
 */
@RestController
@RefreshScope
public class RecommendController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PolicyPopularScheduledService policyPopularScheduledService;
    @Autowired
    private ESClient client;
    @Autowired
    private BehaviorMapper behaviorMapper;
    @Value("${anns.similar.url}")
    private String url;

    @Value("${recommend.url}")
    private String urlForRecommend;

    @Autowired
    private CollectionService collectionService;

    @GetMapping("/recommend/popular")
    public Result popularPolicy(@RequestParam("num") int num) {
        if (num < 0 || num > 1000) {
            return Result.FAIL("num不能大于1000或小于0");
        }
        List<PopularPolicyDTO> policyDTOS = (List<PopularPolicyDTO>) redisTemplate.opsForValue().get(RedisKey.POLICY_POPULAR);
        if (null == policyDTOS) {
            policyDTOS = policyPopularScheduledService.sortPopularPolicy();
        }
        ArrayList<String> pids = new ArrayList<>();
        for (PopularPolicyDTO popularPolicyDTO : policyDTOS.subList(0, num)) {
            pids.add(popularPolicyDTO.getPolicyId());
        }
        return Result.SUCCESS().set("policyIds",pids);
    }

    @GetMapping("/recommend/category/popular")
    public Result categoryPopularPolicy(@RequestParam("num") int num, @RequestParam("categories") ArrayList<String> categories) {
        if (num < 0) {
            return Result.FAIL("num不合理");
        }
        List<PopularPolicyDTO> policyDTOS = (List<PopularPolicyDTO>) redisTemplate.opsForValue().get(RedisKey.POLICY_POPULAR);
        if (null == policyDTOS) {
            policyDTOS = policyPopularScheduledService.sortPopularPolicy();
        }
        ArrayList<String> pids = new ArrayList<>();
        for (PopularPolicyDTO policyDTO : policyDTOS) {
            if (num == 0) {
                break;
            }
            for (String category : categories) {
                if (policyDTO.getCategory().contains(category)) {
                    pids.add(policyDTO.getPolicyId());
                    num--;
                }
            }
        }
        return Result.SUCCESS().set("policyIds",pids);
    }


    @GetMapping("/recommend/home")
    @SneakyThrows
    public Result homeRecommend(@RequestHeader(value = "access_token", required = false) String token) {
        Vector<String> pids = new Vector<>();
        Page<Policy> page = new Page<>();
        page.setCurrent(0);
        page.setSize(15);
        String[] index = {ESIndexConstants.POLICY_INDEX};

        if (StringUtils.isBlank(token)) {
            //用户未登录
            List<PopularPolicyDTO> policyDTOS = (List<PopularPolicyDTO>) redisTemplate.opsForValue().get(RedisKey.POLICY_POPULAR);
            if (null == policyDTOS) {
                policyDTOS = policyPopularScheduledService.sortPopularPolicy();
            }

            for (PopularPolicyDTO popularPolicyDTO : policyDTOS.subList(0, 15)) {
                pids.add(popularPolicyDTO.getPolicyId());
            }
            return client.getPoliciesByIds("uid", page, pids, index, Policy.class);
        }else {
            //用户已登录
            try {
                Claims claims = JWTUtils.parseJWT(token);
                String uid = claims.get("uid").toString();

                // 异步执行 calculate1()
                CompletableFuture<Vector<String>> future1 = CompletableFuture.supplyAsync(() -> {
                    //根据用户的搜索记录进行推荐
                    Vector<String> policyIds = new Vector<>();
                    String pid = behaviorMapper.findPid(Integer.parseInt(uid));
                    if (StringUtils.isNotBlank(pid)) {
                        JSONObject params = JSONUtil.createObj();
                        params.set("target",Integer.parseInt(pid));
                        params.set("num", 5);  //返回最相近的3条政策
                        String result = HttpUtil.post(url+"title_id_search", params.toString());
                        List<Policy> policies = JSONUtil.parseObj(result).getJSONArray("data").toList(Policy.class);
                        for (Policy policy : policies) {
                            policyIds.add(policy.getPolicyId().replace(".0",""));
                        }
                    }
                    return policyIds;
                });

                // 异步执行 calculate2()
                CompletableFuture<Vector<String>> future2 = CompletableFuture.supplyAsync(() -> {
                    JSONObject params1 = JSONUtil.createObj();
                    params1.set("userId",Integer.parseInt(uid));
                    String result = HttpUtil.post(urlForRecommend+"recommend/recommend", params1.toString());
                    List<String> ids = JSONUtil.parseObj(result).getJSONArray("data").toList(String.class);
                    return new Vector<String>(ids.subList(0, 12));
                });

                // 等待两个计算结果完成，并进行相加
                CompletableFuture<Vector<String>> combinedFuture = future1.thenCombine(future2, (res1, res2) -> {
                    res1.addAll(res2);
                    return res1;
                });
                pids = combinedFuture.get();
                Collections.shuffle(pids);
                Result res = client.getPoliciesByIds(uid, page, pids, index, Policy.class);
                return collectionService.collectionList(token,res);

            } catch (Exception e) {
                //相当于用户未登录
                List<PopularPolicyDTO> policyDTOS = (List<PopularPolicyDTO>) redisTemplate.opsForValue().get(RedisKey.POLICY_POPULAR);
                if (null == policyDTOS) {
                    policyDTOS = policyPopularScheduledService.sortPopularPolicy();
                }

                for (PopularPolicyDTO popularPolicyDTO : policyDTOS.subList(0, 15)) {
                    pids.add(popularPolicyDTO.getPolicyId());
                }
                return client.getPoliciesByIds("uid", page, pids, index, Policy.class);
            }
        }

    }

}
