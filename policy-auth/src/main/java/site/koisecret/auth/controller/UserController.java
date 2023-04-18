package site.koisecret.auth.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.auth.dto.RegisterDTO;
import site.koisecret.auth.entity.User;
import site.koisecret.auth.service.UserService;
import site.koisecret.auth.utils.VerifiedCodeUtils;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;

import java.time.Duration;

/**
 * @author by chengsecret
 * @date 2023/3/29.
 */
@RestController
public class UserController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    Result login(@RequestBody RegisterDTO registerDTO) {

        return userService.login(registerDTO.getPhone(),registerDTO.getPassword());
    }

    @GetMapping("/getInfo")
    Result getInfo(@RequestHeader("uid") String uid) {

        return userService.getInfo(Integer.parseInt(uid));
    }

    @PostMapping("/register")
    Result register(@RequestBody RegisterDTO registerDTO) {

        if ("666666".equals(registerDTO.getCode())) {  //用于测试
            return userService.register(registerDTO.getPhone(),registerDTO.getPassword());
        }
        String code = (String) redisTemplate.opsForValue().get(RedisKey.POLICY_CODE + registerDTO.getPhone());
        if (null == code) {
            return Result.FAIL("验证码不存在");
        }
        if (code.equals(registerDTO.getCode())) {
            return userService.register(registerDTO.getPhone(),registerDTO.getPassword());
        }
        return Result.FAIL("验证码错误！");
    }

    @PostMapping("/code")
    Result code(@RequestBody RegisterDTO registerDTO) {
        String phone = registerDTO.getPhone();
        if (StringUtils.isEmpty(phone) || phone.length() != 11) {
            return Result.FAIL("手机号不正确");
        }

        Integer times = (Integer) redisTemplate.opsForValue().get(RedisKey.POLICY_CODE_TIMES + phone);
        if (null == times || times < 10) {  //没人每天只能发10条
            String code = VerifiedCodeUtils.getCode(); //生成验证码
            String s = VerifiedCodeUtils.sentCode(phone, code);
            if ("0".equals(s)) { //验证码发送成功
                String key = RedisKey.POLICY_CODE + phone;
                redisTemplate.opsForValue().set(key, code, Duration.ofSeconds(60));
                if (null == times) {
                    times = 1;
                } else {
                    times++;
                }
                redisTemplate.opsForValue().set(RedisKey.POLICY_CODE_TIMES + phone, times, Duration.ofDays(1));
                return Result.SUCCESS();
            }
        } else {
            return Result.FAIL("发送次数过多，明天再发");
        }

        return Result.FAIL("发送失败");
    }

    @GetMapping("/test")
    Result test(@RequestHeader("uid") String uid) {
        return Result.SUCCESS().set("uid",uid);
    }

    @PostMapping("/update/info")
    Result updateInfo(@RequestHeader("uid") String uid, @RequestBody User user) {
        user.setId(Integer.parseInt(uid));
        return userService.updateInfo(user);
    }


}
