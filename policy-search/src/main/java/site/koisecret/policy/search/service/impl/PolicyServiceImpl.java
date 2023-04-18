package site.koisecret.policy.search.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.es.ESClient;
import site.koisecret.policy.search.es.ESIndexConstants;
import site.koisecret.commons.Response.Page;
import site.koisecret.policy.search.service.PolicyService;

import java.io.IOException;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/2.
 */
@Service
@Slf4j
public class PolicyServiceImpl implements PolicyService {

    @Autowired
    private  ESClient client;


    @Override
    public Result general(Page<Policy> page) throws IOException {
        String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
        return client.generalSearch(page ,index, Policy.class);
    }

    @Override
    public Result getPoliciesByIds(List<String> ids, Page<Policy> page, String uid) throws IOException {
        String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
        return client.getPoliciesByIds(uid, page,ids,index, Policy.class);
    }


}
