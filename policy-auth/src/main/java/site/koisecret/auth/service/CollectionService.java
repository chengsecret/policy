package site.koisecret.auth.service;

import site.koisecret.auth.entity.Collection;
import site.koisecret.commons.Response.Result;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
public interface CollectionService {
    /**
     * 收藏
     * @param collection
     * @return
     */
    public Result addCollection(Collection collection);

    /**
     * 取消收藏
     * @param collection
     * @return
     */
    public Result delCollection(Collection collection);


    public List<String> findPidsByUid(int uid);


}
