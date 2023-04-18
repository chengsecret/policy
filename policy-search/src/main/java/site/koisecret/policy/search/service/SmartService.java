package site.koisecret.policy.search.service;

import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.dto.BaseSearchDTO;

import java.io.IOException;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
public interface SmartService {

    Result smartSearch(BaseSearchDTO baseSearchDTO,String token) throws IOException;

}
