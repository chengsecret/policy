package site.koisecret.policy.search.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;
import site.koisecret.policy.search.entity.Policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author by chengsecret
 * @date 2023/4/2.
 */
@Component
@Slf4j
public class ESClient {
    @Autowired
    private RestHighLevelClient client;

    public <T> Result generalSearch(Page<T> page, String[] index, Class<T> t) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size((int) page.getSize());
        searchSourceBuilder.from((int) page.offset());
        searchSourceBuilder.trackTotalHits(true);

        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder).preference("sessionID"), RequestOptions.DEFAULT);
        List<T> hits = Arrays.stream(searchResponse.getHits().getHits()).map(
                searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), t)
        ).collect(Collectors.toList()); //将es结果映射为java的T类型
        long total = searchResponse.getHits().getTotalHits().value;
        page.setRecords(hits);
        page.setTotal(total);
        return Result.SUCCESS().set("page",page);
    }

    public List<Policy> havePolicyNum(String[] index, String text) throws IOException {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(1);
        TermQueryBuilder termQuery = QueryBuilders.termQuery("pubNumber", text);
        searchSourceBuilder.query(termQuery);

        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder), RequestOptions.DEFAULT);

        List<Policy> hits = Arrays.stream(searchResponse.getHits().getHits()).map(
                searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), Policy.class)
        ).collect(Collectors.toList());

        return hits;
    }



    public Result getPoliciesByIds(String uid, Page<Policy> page, List<String> ids, String[] index, Class<Policy> t) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size((int) page.getSize());
        searchSourceBuilder.from((int) page.offset());
        searchSourceBuilder.trackTotalHits(true);

        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds(ids.toArray(new String[0]));
        searchSourceBuilder.query(idsQueryBuilder);

        // preference保证每个用户使用的分片是同一个，防止结果乱序
        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder).preference(uid), RequestOptions.DEFAULT);
        List<Policy> hits = Arrays.stream(searchResponse.getHits().getHits()).map(
                searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), t)
        ).collect(Collectors.toList()); //将es结果映射为java的T类型
        long total = searchResponse.getHits().getTotalHits().value;
        page.setRecords(hits);
        page.setTotal(total);
        return Result.SUCCESS().set("page",page);
    }

    /**
     * 获取field的聚合值
     * @param field  聚合属性
     * @param size
     * @param index
     * @param province 用于根据省份聚合城市，不用则为null
     * @return
     * @throws IOException
     */
    public ArrayList<String> aggregationField(String field, int size, String[] index, String province) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        if (StringUtils.isNotEmpty(province)) {
            TermQueryBuilder termQuery = QueryBuilders.termQuery("province", province);
            searchSourceBuilder.query(termQuery);
        }

        TermsAggregationBuilder agg = AggregationBuilders.terms(field).field(field).size(size);
        searchSourceBuilder.aggregation(agg);
        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder), RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        Terms aggregation = aggregations.get(field);
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();

        ArrayList<String> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            if (StringUtils.isNotEmpty(bucket.getKeyAsString())) {
                list.add(bucket.getKeyAsString());
            }
        }
        return list;
    }

    /**
     * 聚合属性，并返回数量
     * @param field
     * @param size
     * @param index
     * @return
     */
    public ArrayList<Pair<String, Long>> aggregationFieldWithCount(String field, int size, String[] index) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);

        TermsAggregationBuilder agg = AggregationBuilders.terms(field).field(field).size(size);
        searchSourceBuilder.aggregation(agg);
        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder), RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        Terms aggregation = aggregations.get(field);
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();

        ArrayList<Pair<String, Long>> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            if (StringUtils.isNotEmpty(bucket.getKeyAsString())) {
                Pair<String, Long> pair = new Pair<>(bucket.getKeyAsString(), bucket.getDocCount());
                list.add(pair);
            }
        }
        return list;

    }

    public Result conditionSearch(String[] index, Map<String[], String> multMatchFields, Map<String, Pair<String[], Float>> termFields,
                                  Map<String, Pair<String[], Float>> rangeFilter,Pair<String,Float> pubAgency,Page<Policy> page, int preference) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size((int) page.getSize());
        searchSourceBuilder.from((int) page.offset());
        searchSourceBuilder.trackTotalHits(true);

        highlightField(searchSourceBuilder,"policyTitle", "policyBody", "policySource");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //可分词
        for (String[] fields : multMatchFields.keySet()) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(multMatchFields.get(fields), fields));
        }

        //不可分词
        for (String field : termFields.keySet()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(field,termFields.get(field).getKey()).boost(termFields.get(field).getValue()));
        }

        //时间范围
        for (String key : rangeFilter.keySet()) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(key);
            String dateFrom = rangeFilter.get(key).getKey()[0];
            String dateTo = rangeFilter.get(key).getKey()[1];
            if (StringUtils.isNotBlank(dateFrom)) {
                rangeQueryBuilder.from(dateFrom);
            }
            if (StringUtils.isNotBlank(dateTo)) {
                rangeQueryBuilder.to(dateTo);
            }
            boolQueryBuilder.must(rangeQueryBuilder).boost(rangeFilter.get(key).getValue());
        }

        //处理pubAgency
        if (null != pubAgency) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.should(QueryBuilders.matchQuery("pubAgencyFullName",pubAgency.getKey()).boost(pubAgency.getValue()));
            queryBuilder.should(QueryBuilders.termQuery("pubAgency",pubAgency.getKey()).boost(pubAgency.getValue()));
            boolQueryBuilder.must(queryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        // preference保证每个用户使用的分片是同一个，防止结果乱序
        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder).preference(String.valueOf(preference)), RequestOptions.DEFAULT);

        List<Policy> hits = Arrays.stream(searchResponse.getHits().getHits()).map(
                searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), Policy.class)
        ).collect(Collectors.toList()); //将es结果映射为java的T类型
        long total = searchResponse.getHits().getTotalHits().value;
        page.setRecords(hits);
        page.setTotal(total);
        Result result = Result.SUCCESS().set("page", page);

        List<Dict> marks = getSearchHighlight(searchResponse, new String[]{"policyTitle", "policyBody", "policySource"});
        result.set("marks", marks);

        return result;
    }

    public Result smartSearch(String[] index, String[] fields, String field, String text, Page<Policy> page, int preference) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size((int) page.getSize());
        searchSourceBuilder.from((int) page.offset());
        searchSourceBuilder.trackTotalHits(true);

        if (StringUtils.isNotEmpty(field)) {
            TermQueryBuilder termQuery = QueryBuilders.termQuery(field, text);
            searchSourceBuilder.query(termQuery);
        }
        if (StringUtils.isNotBlank(text) && null != fields) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(text, fields);
            searchSourceBuilder.query(multiMatchQueryBuilder);
        }

//        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder), RequestOptions.DEFAULT);
        SearchResponse searchResponse = client.search(new SearchRequest(index).source(searchSourceBuilder).preference(String.valueOf(preference)), RequestOptions.DEFAULT);
        List<Policy> hits = Arrays.stream(searchResponse.getHits().getHits()).map(
                searchHit -> BeanUtil.toBean(searchHit.getSourceAsMap(), Policy.class)
        ).collect(Collectors.toList()); //将es结果映射为java的T类型
        long total = searchResponse.getHits().getTotalHits().value;
        page.setRecords(hits);
        page.setTotal(total);
        return Result.SUCCESS().set("page",page);
    }

    /**
     * 设置高亮
     */
    private void highlightField(SearchSourceBuilder searchSourceBuilder, String... fields) {
        String preTag = "<font style='color:#EC7A08'>";
        String postTag = "</font>";
        HighlightBuilder highlightBuilder = new HighlightBuilder().preTags(preTag).postTags(postTag);
        for (String field : fields) {
            highlightBuilder.field(new HighlightBuilder.Field(field));
        }
        searchSourceBuilder.highlighter(highlightBuilder);
    }

    private List<Dict> getSearchHighlight(SearchResponse searchResponse, String[] matchFields) throws IOException {
        return Arrays.stream(searchResponse.getHits().getHits()).map(searchHit -> {
            Dict highlight = Dict.create();
            for (String field : matchFields) {
                HighlightField highlightField = searchHit.getHighlightFields().get(field);
                if (highlightField != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Text fragment : highlightField.getFragments()) {
                        sb.append(fragment.toString());
                    }
                    highlight.set(field, sb.toString());
                }
            }
            return highlight;
        }).collect(Collectors.toList());
    }

}
