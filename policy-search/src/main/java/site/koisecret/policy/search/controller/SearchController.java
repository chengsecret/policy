package site.koisecret.policy.search.controller;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.BaseSearchDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.entity.SearchHistory;
import site.koisecret.policy.search.mapper.SearchHistoryMapper;
import site.koisecret.policy.search.service.CollectionService;
import site.koisecret.policy.search.service.PolicyService;
import site.koisecret.policy.search.service.SmartService;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author by chengsecret
 * @date 2023/3/28.
 */
@RestController
public class SearchController {

    @Autowired
    private PolicyService policyService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    @Autowired
    private SmartService smartService;

    @Value("${anns.similar.url}")
    private String url;

    /**
     * 检索的通用接口，前端测试用
     *
     * @param page
     * @return
     */
    @GetMapping("/general")
    @SneakyThrows
    Result general(Page<Policy> page, @RequestHeader(value = "access_token", required = false) String token) {
        Result result = policyService.general(page);
        if (StringUtils.isEmpty(token)) {  //用户未登录
            return result;
        }
        return collectionService.collectionList(token,result);
    }

    /**
     * 传入政策id，返回最相近的n条专利+
     * @param policyId 政策id policyID
     * @return
     */
    @HystrixCommand(fallbackMethod = "similarPolicyFallBack")
    @GetMapping("/similar/{id}")
    @SneakyThrows
    Result getSimilarById(@PathVariable("id") String policyId, Page<Policy> page,
                          @RequestHeader(value = "access_token", required = false) String token) {
        JSONObject params = JSONUtil.createObj();
        params.set("target",Integer.parseInt(policyId));
        params.set("num", 50);  //返回最相近的50条政策
        page.setTotal(50);

        String result = HttpUtil.post(url+"title_id_search", params.toString());

        List<Policy> policies = JSONUtil.parseObj(result).getJSONArray("data").toList(Policy.class);
        long offset = page.offset();
        if (offset < policies.size()) {
            policies =  policies.subList((int) offset, (int) Math.min(offset + page.getSize(), policies.size()));
            for (Policy policy : policies) {
                policy.setPolicyId(String.valueOf((int) (Float.parseFloat(policy.getPolicyId()))));
            }
            page.setRecords(policies);
        }
        Result res = Result.SUCCESS().set("page", page);

        if (StringUtils.isEmpty(token)) {  //用户未登录
            return res;
        }
        return collectionService.collectionList(token,res);
    }

    @HystrixCommand(fallbackMethod = "semanticPolicyFallBack")
    @PostMapping("/semantic")
    @SneakyThrows
    Result semanticSearch(@RequestHeader("uid") String uid, @RequestBody BaseSearchDTO baseSearchDTO) {
        Page<Policy> page = new Page<>();
        page.setSize(baseSearchDTO.getSize());
        page.setCurrent(baseSearchDTO.getCurrent());

        if (StringUtils.isNotBlank(baseSearchDTO.getText())) {
            //保存搜索记录
            threadPoolExecutor.execute(()->{
                SearchHistory searchHistory = new SearchHistory(-1,Integer.parseInt(uid),
                        null,
                        baseSearchDTO.getText(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        2  //0表示 智能搜索
                );
                searchHistoryMapper.addSearchHistory(searchHistory);
            });
            JSONObject params = JSONUtil.createObj();
            params.set("target", baseSearchDTO.getText());
            params.set("num", 50);  //返回最相近的50条政策
            page.setTotal(50);

            String result = HttpUtil.post(url+"title_search", params.toString());
            List<Policy> policies = JSONUtil.parseObj(result).getJSONArray("data").toList(Policy.class);
            long offset = page.offset();
            if (offset < policies.size()) {
                policies =  policies.subList((int) offset, (int) Math.min(offset + page.getSize(), policies.size()));
                for (Policy policy : policies) {
                    policy.setPolicyId(String.valueOf((int) (Float.parseFloat(policy.getPolicyId()))));
                }
                page.setRecords(policies);
            }
            Result res = Result.SUCCESS().set("page", page);
//            Result res =  smartService.smartSearch(baseSearchDTO, null);
            return collectionService.collectionListHaveAuth(uid, res);
        }else {
            Result result = policyService.general(page);

            return collectionService.collectionListHaveAuth(uid,result);
        }


    }


    @GetMapping("/getPolicies")
    @SneakyThrows
    Result getPoliciesByids(@RequestParam("ids") List<String> ids, @RequestParam("current") long current,
                            @RequestParam("size") long size, @RequestParam("uid") String uid) {
        Page<Policy> page = new Page<>();
        page.setCurrent(current);
        page.setSize(size);
        return policyService.getPoliciesByIds(ids,page, uid);
    }


    /**
     * 指定统一的降级方法
     * 参数：没有参数
     * @return
     */
    public Result similarPolicyFallBack(String policyId, Page<Policy> page, String uid) {
        return Result.FAIL("服务暂不可用");
    }

    public Result semanticPolicyFallBack(String uid, BaseSearchDTO baseSearchDTO) {
        return Result.FAIL("服务暂不可用");
    }


}
