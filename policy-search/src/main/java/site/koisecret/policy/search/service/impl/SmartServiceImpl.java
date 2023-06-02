package site.koisecret.policy.search.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.commons.redisKey.RedisKey;
import site.koisecret.commons.utils.JWTUtils;
import site.koisecret.policy.search.dto.BaseSearchDTO;
import site.koisecret.policy.search.entity.Policy;
import site.koisecret.policy.search.entity.SearchHistory;
import site.koisecret.policy.search.es.ESClient;
import site.koisecret.policy.search.es.ESIndexConstants;
import site.koisecret.policy.search.mapper.SearchHistoryMapper;
import site.koisecret.policy.search.service.SmartService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@Service
@Slf4j
@RefreshScope
public class SmartServiceImpl implements SmartService {
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    @Autowired
    private ESClient client;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${anns.similar.url}")
    private String url;

    @Override
    public Result smartSearch(BaseSearchDTO baseSearchDTO, String token) throws IOException {
        //有token保存搜索记录
        if (StringUtils.isNotEmpty(token) && StringUtils.isNotBlank(baseSearchDTO.getText())) {
            threadPoolExecutor.execute(()->{
                try {
                    Claims claims = JWTUtils.parseJWT(token);
                    String uid = claims.get("uid").toString();
                    SearchHistory searchHistory = new SearchHistory(-1,Integer.parseInt(uid),
                            null,
                            baseSearchDTO.getText(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0  //0表示 智能搜索
                    );
                    searchHistoryMapper.addSearchHistory(searchHistory);
                } catch (ExpiredJwtException expiredJwtEx) {
                    log.info("token : {} 过期", token );
                } catch (Exception e) {
                    log.info("token : {} 验证失败 or 保存搜索记录失败" , token );
                }
            });
        }

        Page<Policy> page = new Page<>();
        page.setCurrent(baseSearchDTO.getCurrent());
        page.setSize(baseSearchDTO.getSize());


        if (null != baseSearchDTO.getText() && baseSearchDTO.getText().length() > 20) {
            JSONObject params = JSONUtil.createObj();
            params.set("target", baseSearchDTO.getText());
            params.set("num", 50);  //返回最相近的50条政策
            page.setTotal(50);

            String result = HttpUtil.post(url+"title_search", params.toString());
            List<Policy> policies = JSONUtil.parseObj(result).getJSONArray("data").toList(Policy.class);
            long offset = page.offset();
            if (offset < policies.size()) {
                policies =  policies.subList((int) offset, (int) Math.min(offset + page.getSize(), policies.size()));
                for (Policy policy : policies) {
                    policy.setPolicyId(String.valueOf((int) (Float.parseFloat(policy.getPolicyId()))));
                }
                page.setRecords(policies);
            }
            return Result.SUCCESS().set("page", page);
        }

        String[] index = {ESIndexConstants.POLICY_INDEX};
        String[] fields = null;
        String field = index(baseSearchDTO.getText());

        if (null == field) {
            //判断输入内容是否为 发布字号
            List<Policy> policies = client.havePolicyNum(index,baseSearchDTO.getText());
            if (null != policies && policies.size() != 0) {
                page.setTotal(1);
                page.setRecords(policies);
                return Result.SUCCESS().set("page",page);
            }
            fields = new String[]{"policyTitle","pubAgencyFullName","policyBody","policySource"};
        }

        return client.smartSearch(index,fields, field,baseSearchDTO.getText(),page,baseSearchDTO.hashCode());
    }

    /**
     * 搜索索引，有结果就返回属性，否则返回null
     * @param text
     * @return
     */
    private String index(String text) throws IOException {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        if ("省级".equals(text) || "国家级".equals(text) || "区县级".equals(text) || "市级".equals(text)) {
            return "policyGrade";
        }
        ArrayList<String> agencies = (ArrayList<String>) redisTemplate.opsForValue().get(RedisKey.POLICY_AGENCY);
        if (null == agencies) {
            agencies = client.aggregationField("pubAgency", 7000, new String[]{ESIndexConstants.POLICY_INDEX}, null);
            redisTemplate.opsForValue().set(RedisKey.POLICY_AGENCY,agencies);
        }
        if (agencies.contains(text)) {
            return "pubAgency";
        }
        return null;
    }

}
