package site.koisecret.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.koisecret.commons.Response.Result;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@FeignClient(name = "policy-search", fallback = PolicySearchFeignClientCallback.class)
@Component
public interface PolicySearchFeignClient {
    /**
     * 根据pid获取政策列表
     * @param ids
     * @param
     * @return
     */
    @GetMapping("/getPolicies")
    Result getPoliciesByids(@RequestParam("ids") List<String> ids,@RequestParam("current") long current,
                            @RequestParam("size") long size, @RequestParam("uid") String uid);
}
