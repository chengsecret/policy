package site.koisecret.policy.search.service;

import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.commons.Response.Page;

import java.io.IOException;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/2.
 */
public interface PolicyService {

    /**
     * 通用接口，用于测试
     * @param page
     * @return
     * @throws IOException
     */
    Result general(Page<Policy> page) throws IOException;


    Result getPoliciesByIds(List<String> ids, Page<Policy> page, String uid) throws IOException;

}
