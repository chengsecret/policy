package site.koisecret.esoperation.service;

import cn.hutool.core.bean.BeanUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.koisecret.esoperation.entity.PolicySql;
import site.koisecret.esoperation.mapper.PolicyMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@Service
public class ToMysql {
    private static final int BATCH_SIZE = 1000; // 批量操作的文档数量
    private static final int THREAD_POOL_SIZE = 8; // 线程池大小


    @Autowired
    private PolicyMapper policyMapper;
    @Autowired
    private RestHighLevelClient client;

    public void write() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<String[]> lines = readTsvFile();
        int size = lines.size();
        int batchCount = size / BATCH_SIZE + 1;
        for (int i = 0; i < batchCount; i++) {

            int startIndex = i * BATCH_SIZE;
            int endIndex = Math.min(startIndex + BATCH_SIZE, size);
            List<String[]> batchLines = lines.subList(startIndex, endIndex);
            executorService.execute(() -> {
                List<String> pids = new ArrayList<>();
                for (String[] line : batchLines) {
                    pids.add(line[0]);
                }
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.size(pids.size());
                searchSourceBuilder.trackTotalHits(true);
                IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds(pids.toArray(new String[0]));
                searchSourceBuilder.query(idsQueryBuilder);
                SearchResponse searchResponse = null;
                try {
                    searchResponse = client.search(new SearchRequest("policy").source(searchSourceBuilder), RequestOptions.DEFAULT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                List<PolicySql> policies = Arrays.stream(searchResponse.getHits().getHits()).map(
                        searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), PolicySql.class)
                ).collect(Collectors.toList());
                System.out.println(policies.size());
                for (PolicySql policy : policies) {
                    policyMapper.addPolicy(policy);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }


    private List<String[]> readTsvFile() throws IOException {
        List<String[]> lines = new ArrayList<>();
        File file = new File("policyinfo.tsv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            lines.add(fields);
        }
        reader.close();
        return lines;
    }
}
