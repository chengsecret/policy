package site.koisecret.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.auth.mapper.CategoryMapper;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
@RestController
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/get/industry")
    public Result getIndustry() {
        List<String> industries = (List<String>) redisTemplate.opsForValue().get(RedisKey.INDUSTRY);
        if (null == industries) {
            industries = categoryMapper.getIndustries();
            redisTemplate.opsForValue().set(RedisKey.INDUSTRY, industries);
        }
        return Result.SUCCESS().set("industry", industries);
    }

    @GetMapping("/get/profession")
    public Result getProfession() {
        List<String> professions = (List<String>) redisTemplate.opsForValue().get(RedisKey.PROFESSION);
        if (null == professions) {
            professions = categoryMapper.getProfessions();
            redisTemplate.opsForValue().set(RedisKey.PROFESSION, professions);
        }
        return Result.SUCCESS().set("profession", professions);
    }




}
