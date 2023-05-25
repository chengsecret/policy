package site.koisecret.policy.search.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.commons.utils.JWTUtils;
import site.koisecret.policy.search.dto.ConditionSearchDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.entity.SearchHistory;
import site.koisecret.policy.search.es.ESClient;
import site.koisecret.policy.search.es.ESIndexConstants;
import site.koisecret.policy.search.mapper.PolicyTypeMapper;
import site.koisecret.policy.search.mapper.SearchHistoryMapper;
import site.koisecret.policy.search.service.ConditionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
@Service
@Slf4j
public class ConditionServiceImpl implements ConditionService {

    @Autowired
    private ESClient client;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PolicyTypeMapper policyTypeMapper;
    @Autowired
    SearchHistoryMapper searchHistoryMapper;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Result getProvince() throws IOException {
        ArrayList<String> province = (ArrayList<String>)redisTemplate.opsForValue().get(RedisKey.PROVINCE);
        if (null == province) {
            String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
            province = client.aggregationField("province", 50, index, null);
            redisTemplate.opsForValue().set(RedisKey.PROVINCE, province);
        }
        return Result.SUCCESS().set("province",province);
    }

    @Override
    public Result getCityByProvince(String province) throws IOException {

        ArrayList<String> city = (ArrayList<String>)redisTemplate.opsForValue().get(RedisKey.CITY_OF_PROVINCE + province);
        if (null == city) {
            String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
            city = client.aggregationField("city", 50, index, province);
            redisTemplate.opsForValue().set(RedisKey.CITY_OF_PROVINCE + province, city);
        }

        return Result.SUCCESS().set("city",city);
    }

    @Override
    public Result getConditions() throws IOException {

        List<String> policyCategory = (List<String>) redisTemplate.opsForValue().get(RedisKey.POLICY_CATEGORY);
        if (null == policyCategory) {
            policyCategory = policyTypeMapper.getPolicyType();
            redisTemplate.opsForValue().set(RedisKey.POLICY_CATEGORY, policyCategory);
        }
        Result result = Result.SUCCESS().set("policyCategory", policyCategory);

        ArrayList<String> policyGrade = (ArrayList<String>) redisTemplate.opsForValue().get(RedisKey.POLICY_GRADE);
        if (null == policyGrade) {
            String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
            policyGrade = client.aggregationField("policyGrade", 10, index, null);
            redisTemplate.opsForValue().set(RedisKey.POLICY_GRADE, policyGrade);
        }
        result.set("policyGrade", policyGrade);

        ArrayList<String> policyType = (ArrayList<String>) redisTemplate.opsForValue().get(RedisKey.POLICY_TYPE);
        if (null == policyType) {
            String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
            policyType = client.aggregationField("policyType", 50, index, null);
            redisTemplate.opsForValue().set(RedisKey.POLICY_TYPE, policyType);
        }
        result.set("policyType", policyType);

        ArrayList<String> province = (ArrayList<String>)redisTemplate.opsForValue().get(RedisKey.PROVINCE);
        if (null == province) {
            String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
            province = client.aggregationField("province", 50, index, null);
            redisTemplate.opsForValue().set(RedisKey.PROVINCE, province);
        }
        result.set("province", province);

        return result;
    }

    @Override
    public Result conditionSearch(String token, ConditionSearchDTO condition) throws IOException {
        //有token保存搜索记录
        if (StringUtils.isNotEmpty(token)) {
            threadPoolExecutor.execute(()->{
                try {
                    Claims claims = JWTUtils.parseJWT(token);
                    String uid = claims.get("uid").toString();
                    SearchHistory searchHistory = new SearchHistory(-1,Integer.parseInt(uid),
                            Arrays.toString(condition.getCategory()),
                            condition.getText(),
                            Arrays.toString(condition.getGrade()),
                            condition.getAgency(),
                            Arrays.toString(condition.getType()),
                            Arrays.toString(condition.getProvince()),
                            condition.getStartDate(),
                            condition.getEndDate(),
                            condition.getPubNumber(),
                            1  //1表示 条件搜索
                    );
                    searchHistoryMapper.addSearchHistory(searchHistory);
                } catch (ExpiredJwtException expiredJwtEx) {
                    log.info("token : {} 过期", token );
                } catch (Exception e) {
                    log.info("token : {} 验证失败 or 保存搜索记录失败" , token );
                }
            });
        }

        String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
        Page<Policy> page = new Page<>();
        page.setSize(condition.getSize());
        page.setCurrent(condition.getCurrent());
        //用于title、body policySource的查询，map的key为属性，value为值
        Map<String[], String> multMatchFields = new HashMap<>(8);
        //用于不分词属性的查询，map的key为属性，pair中key为查询的值，value为权重
        Map<String, Pair<String[], Float>> termFields = new HashMap<>(8);
        // 拼接范围查询
        Map<String, Pair<String[], Float>> rangeFilter = new HashMap<>(8);
        //处理es中发布机构与发布机构全名
        Pair<String,Float> pubAgency = null;

        if (StringUtils.isNotBlank(condition.getAgency())) {
            pubAgency = new Pair<>(condition.getAgency().trim(),getWeight(condition.getAgencyWeight()));
        }

        if (StringUtils.isNotBlank(condition.getText())) {
            multMatchFields.put(new String[]{"policyTitle", "policyBody", "policySource"}, condition.getText());
        }

        if (null != condition.getCategory() && condition.getCategory().length != 0) {
            termFields.put("category", new Pair<>(condition.getCategory(),getWeight(condition.getCategoryWeight())));
        }

        if (null != condition.getProvince() && condition.getProvince().length != 0) {
            termFields.put("province",new Pair<>(condition.getProvince(),getWeight(condition.getProvinceWeight())));
        }

        if (StringUtils.isNotBlank(condition.getPubNumber())) {
            String[] pubNumber = condition.getPubNumber().trim().replaceAll(" + ", " ").split(" ");
            termFields.put("pubNumber",new Pair<>(pubNumber,getWeight(condition.getPubNumberWeight())));
        }

        if (null != condition.getGrade() && condition.getGrade().length != 0) {
            termFields.put("policyGrade",new Pair<>(condition.getGrade(),getWeight(condition.getGradeWeight())));
        }

        if (null != condition.getType() && condition.getType().length != 0) {
            termFields.put("policyType",new Pair<>(condition.getType(),getWeight(condition.getTypeWeight())));
        }

        if (StringUtils.isNotBlank(condition.getStartDate()) || StringUtils.isNotBlank(condition.getEndDate())) {
            String[] date = new String[]{condition.getStartDate(),condition.getEndDate()};
            rangeFilter.put("pubTime",new Pair<>(date,getWeight(condition.getDateWeight())));
        }

        return client.conditionSearch(index,multMatchFields,termFields,rangeFilter,pubAgency, page, condition.hashCode());
    }

    @Override
    public Result provinceNum() throws IOException {
        String[] index = new String[]{ESIndexConstants.POLICY_INDEX};
        ArrayList<Pair<String, Long>> province = client.aggregationFieldWithCount("province", 50, index);
        return Result.SUCCESS().set("data",province);
    }

    private float getWeight(String text) {
        if ("低".equals(text)) {
            return 0.1f;
        } else if ("高".equals(text)) {
            return 1.9f;
        }else{
            return 1.0f;
        }
    }
}
