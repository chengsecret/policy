package site.koisecret.policy.search.service;

import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.ConditionSearchDTO;

import java.io.IOException;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
public interface ConditionService {

    Result getProvince() throws IOException;

    Result getCityByProvince(String province) throws IOException;

    Result getConditions() throws IOException;

    Result conditionSearch(String token, ConditionSearchDTO conditionSearchDTO) throws IOException;

    Result provinceNum() throws IOException;
}
