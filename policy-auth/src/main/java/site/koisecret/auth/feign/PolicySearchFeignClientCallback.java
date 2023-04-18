package site.koisecret.auth.feign;

import org.springframework.stereotype.Component;
import site.koisecret.commons.Response.Result;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/4.
 */
@Component
public class PolicySearchFeignClientCallback implements PolicySearchFeignClient  {
    @Override
    public Result getPoliciesByids(List<String> ids, long current, long size, String uid) {
        return Result.FAIL("服务暂不可用，稍后再试");
    }
}
