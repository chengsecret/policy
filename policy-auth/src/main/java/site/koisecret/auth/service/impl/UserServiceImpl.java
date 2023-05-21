package site.koisecret.auth.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.koisecret.auth.entity.User;
import site.koisecret.auth.mapper.UserMapper;
import site.koisecret.auth.service.UserService;
import site.koisecret.auth.utils.VerifiedCodeUtils;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.utils.JWTUtils;

import java.util.Date;
import java.util.HashMap;

/**
 * @author by chengsecret
 * @date 2023/3/31.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public Result register(String phone, String password) {

        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(phone)) {
            return Result.FAIL("未输入手机或密码");
        }

        if (userMapper.findUserByPhone(phone) != null) {
            return Result.FAIL("用户已存在");
        }

        String name = VerifiedCodeUtils.getRandomString(5);
        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setName(name);
        user.setAge(25);
        user.setProvince("浙江省");
        user.setCity("杭州市");
        user.setIndustry("信息传输、软件和信息技术服务业");
        user.setProfession("学生");
        if (userMapper.addUser(user)) {
            User userByPhone = userMapper.findUserByPhone(phone);
            HashMap<String, Object> jwtClaims = new HashMap<String, Object>() {{
                //原本token中放的是openid，现在将openid作为Payload（有效载荷）
                put("uid", userByPhone.getId());
            }};
            Date expDate = DateTime.now().plusDays(7).toDate(); //过期时间 7 天

            String token = JWTUtils.createJWT(expDate, jwtClaims);

            Result result = Result.SUCCESS().set("token", token);
            userByPhone.setPassword("bugaosuni");
            result.set("info",userByPhone);
            return result ;
        }
        return Result.FAIL("注册失败，稍后再试");
    }

    @Override
    public Result login(String phone, String password) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(phone)) {
            return Result.FAIL("请输入账号密码");
        }
        User user = userMapper.findUserByPhone(phone);
        if (null == user) {
            return Result.FAIL("用户不存在，请重新输入手机号");
        }else {
            if (password.equals(user.getPassword())) {
                //登录成功，返回token
                HashMap<String, Object> jwtClaims = new HashMap<String, Object>() {{
                    //原本token中放的是openid，现在将openid作为Payload（有效载荷）
                    put("uid", user.getId());
                }};
                Date expDate = DateTime.now().plusDays(7).toDate(); //过期时间 7 天

                String token = JWTUtils.createJWT(expDate, jwtClaims);

                Result result = Result.SUCCESS().set("token", token);
                user.setPassword("bugaosuni");
                result.set("info", user);

                return result;

            }else {
                return Result.FAIL("用户名或密码错误");
            }
        }
    }

    @Override
    public Result updateInfo(User user) {
        if (userMapper.updateUser(user)) {
            return Result.SUCCESS();
        }
        return Result.FAIL();
    }

    @Override
    public Result getInfo(int uid) {
        User user = userMapper.findUserById(uid);
        user.setPassword("bugaosui");
        return Result.SUCCESS().set("info", user);
    }
}
