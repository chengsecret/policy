package site.koisecret.policy.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.policy.search.dto.PopularPolicyDTO;
import site.koisecret.policy.search.service.impl.PolicyPopularScheduledService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/5/21.
 */
@RestController
public class RecommendController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PolicyPopularScheduledService policyPopularScheduledService;

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

//    @GetMapping("/recommend/browse")
//    public Result browse(@RequestParam("num") int num) {
//        JSONObject params = JSONUtil.createObj();
////        params.set("target",Integer.parseInt(policyId));
////        params.set("num", 50);  //返回最相近的50条政策
////        page.setTotal(50);
////
////        String result = HttpUtil.post(url+"bert_id_search", params.toString());
////
////        List<Policy> policies = JSONUtil.parseObj(result).getJSONArray("data").toList(Policy.class);
////        long offset = page.offset();
////        if (offset < policies.size()) {
////            policies =  policies.subList((int) offset, (int) Math.min(offset + page.getSize(), policies.size()));
////            for (Policy policy : policies) {
////                policy.setPolicyId(String.valueOf((int) (Float.parseFloat(policy.getPolicyId()))));
////            }
////            page.setRecords(policies);
////        }
////        Result res = Result.SUCCESS().set("page", page);
//
//        return Result.SUCCESS().set("num", num);
//    }

}
