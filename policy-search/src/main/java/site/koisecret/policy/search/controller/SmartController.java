package site.koisecret.policy.search.controller;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.BaseSearchDTO;
import site.koisecret.policy.search.service.CollectionService;
import site.koisecret.policy.search.service.SmartService;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@RestController
public class SmartController {
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private SmartService smartService;

    @PostMapping("/smart")
    @SneakyThrows
    Result smartSearch(@RequestBody BaseSearchDTO baseSearchDTO, @RequestHeader(value = "access_token", required = false) String token) {

        Result result = smartService.smartSearch(baseSearchDTO, token);
        if (StringUtils.isBlank(token)) {  //用户未登录
            return result;
        }
        return collectionService.collectionList(token,result);
    }

}
