package site.koisecret.policy.search.controller;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.ConditionSearchDTO;
import site.koisecret.policy.search.service.CollectionService;
import site.koisecret.policy.search.service.ConditionService;

/**
 * 条件搜索
 *
 * @author by chengsecret
 * @date 2023/4/7.
 */
@RestController
public class ConditionController {

    @Autowired
    private ConditionService conditionService;
    @Autowired
    private CollectionService collectionService;

    @GetMapping("get/province")
    @SneakyThrows
    Result getProvince() {
        return conditionService.getProvince();
    }

    @GetMapping("/province/num")
    @SneakyThrows
    Result provinceNum() {
        return conditionService.provinceNum();
    }

    @GetMapping("get/city")
    @SneakyThrows
    Result getCity(String province) {
        return conditionService.getCityByProvince(province);
    }

    @GetMapping("get/conditions")
    @SneakyThrows
    Result getConditions() {
        return conditionService.getConditions();
    }

    @PostMapping("condition")
    @SneakyThrows
    Result conditonSearch(@RequestBody ConditionSearchDTO conditionSearchDTO, @RequestHeader(value = "access_token", required = false) String token) {
        Result result = conditionService.conditionSearch(token, conditionSearchDTO);
        if (StringUtils.isEmpty(token)) {  //用户未登录
            return result;
        }
        return collectionService.collectionList(token,result);
    }

}
