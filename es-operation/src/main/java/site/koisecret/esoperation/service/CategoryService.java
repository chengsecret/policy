package site.koisecret.esoperation.service;

import io.micrometer.core.instrument.util.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
@Service
public class CategoryService {
    @Autowired
    private RestHighLevelClient client;

    /**
     *
     * @param text 搜索的文本
     * @param type 类型 见policy_type表
     * @param map  key为policyId，值为 政策类型数组
     * @throws IOException
     */
    public void category(String text, String type, HashMap<String, ArrayList<String>> map) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);

        String[] fields = new String[]{"policyTitle", "policyBody"};
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(text, fields);
        searchSourceBuilder.query(multiMatchQuery);


        TermsAggregationBuilder agg = AggregationBuilders.terms("policyId").field("policyId.keyword").size(65000);
        searchSourceBuilder.aggregation(agg);
        SearchResponse searchResponse = client.search(new SearchRequest("policy").source(searchSourceBuilder), RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        Terms aggregation = aggregations.get("policyId");
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();

        ArrayList<String> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            if (StringUtils.isNotEmpty(bucket.getKeyAsString())) {
                list.add(bucket.getKeyAsString());
            }
        }

        System.out.println(text + "   " + list.size());

        for (String id : list) {
            ArrayList<String> integers = map.get(id);
            if (null == integers) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(type);
                map.put(id,arrayList);
            }else {
                integers.add(type);
            }
        }
    }

    public void addCategory(HashMap<String, ArrayList<String>> map) {
        int i = 1;
        Set<String> keySet = map.keySet();
        for (String pid : keySet) {
            UpdateRequest updateRequest = new UpdateRequest("policy", pid);
            Map<String, Object> updateObject = new HashMap<>();
            System.out.println(Arrays.toString(map.get(pid).toArray(new String[0])));
            updateObject.put("category", map.get(pid));

            // 执行IndexRequest请求，获取IndexResponse响应对象
            try {
                updateRequest.doc(updateObject);
                client.update(updateRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                if (!e.getMessage().contains("200 OK")) {
                    throw new RuntimeException(e);
                }else {
                    System.out.println(i++);
                    System.out.println(pid);
                }
            }
        }
    }

    //将对应pid的政策的category设为空
    public void updateCategory(String pid) {
        UpdateRequest updateRequest = new UpdateRequest("policy", pid);
        Map<String, Object> updateObject = new HashMap<>();
        updateObject.put("category", new ArrayList<String>());
        try {
            updateRequest.doc(updateObject);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            if (!e.getMessage().contains("200 OK")) {
                throw new RuntimeException(e);
            }else {
                System.out.println(pid);
            }
        }
    }

    public void updateBody(String pid, String body) {
        UpdateRequest updateRequest = new UpdateRequest("policy", pid);
        Map<String, Object> updateObject = new HashMap<>();
        updateObject.put("policyBody", body);
        try {
            updateRequest.doc(updateObject);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            if (!e.getMessage().contains("200 OK")) {
                throw new RuntimeException(e);
            }else {
                System.out.println(pid);
            }
        }
    }




}
