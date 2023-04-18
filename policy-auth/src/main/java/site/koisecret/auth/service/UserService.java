package site.koisecret.auth.service;

import site.koisecret.auth.entity.User;
import site.koisecret.commons.Response.Result;

/**
 * @author by chengsecret
 * @date 2023/3/31.
 */
public interface UserService {
    /**
     * 注册
     * @param phone
     * @param password
     * @return
     */
    public Result register(String phone,String password);

    /**
     * 登录
     * @param phone
     * @param password
     * @return
     */
    public Result login(String phone,String password);

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    public Result updateInfo(User user);

    public Result getInfo(int uid);
}
