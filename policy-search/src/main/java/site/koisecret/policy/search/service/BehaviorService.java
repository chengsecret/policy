package site.koisecret.policy.search.service;

import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.entity.Policy;

import java.io.IOException;

/**
 * @author by chengsecret
 * @date 2023/4/6.
 */
public interface BehaviorService {

    /**
     * 获取浏览记录
     * @param uid
     * @return
     */
    public Result browseRecord(int uid, Page<Policy> page) throws IOException;

    /**
     * 添加浏览记录
     * @param uid
     * @param pid
     * @return
     */
    public Result addBehavior(int uid, String pid);
}
