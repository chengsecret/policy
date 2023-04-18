package site.koisecret.policy.search.service;

import site.koisecret.commons.Response.Result;

/**
 * @author by chengsecret
 * @date 2023/4/4.
 */
public interface CollectionService {
    /**
     * 无需登录就能请求的接口， 判断Result中的政策是否被收藏
     * @param token
     * @param result
     * @return
     */
    Result collectionList(String token, Result result);

    /**
     * 必须登录才能访问的接口，判断Result中的政策是否被收藏
     * @param uid
     * @param result
     * @return
     */
     Result collectionListHaveAuth(String uid, Result result);
}
