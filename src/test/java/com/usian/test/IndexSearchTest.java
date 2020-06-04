package com.usian.test;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class IndexSearchTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private SearchRequest searchRequest;
    private SearchResponse searchResponse;

    @Before
    public void initSearchRequest() {
        // 搜索请求对象
        searchRequest = new SearchRequest("java1906");
        searchRequest.types("course");
    }

    // 搜索type下的全部记录
    @Test
    public void testSearchAll() throws Exception {

        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        // 设置搜索源
        searchRequest.source(searchSourceBuilder);
        // 执行搜索
        searchResponse = 
            restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

    }

    @After
    public void displayDoc() throws ParseException {
        // 搜索匹配结果
        SearchHits hits = searchResponse.getHits();
        // 搜索总记录数
        long totalHits = hits.totalHits;
        System.out.println("共搜索到" + totalHits + "条文档");
        // 匹配的文档
        SearchHit[] searchHits = hits.getHits();
        // 日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            // 文档id
            String id = hit.getId();
            System.out.println("id：" + id);
            // 源文档内容
            String source = hit.getSourceAsString();
            System.out.println(source);
        }
    }

    //分页查询
    @Test
    public void testSearchPage() throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(1);
        searchSourceBuilder.size(5);
        searchSourceBuilder.sort("price", SortOrder.ASC);

        // 设置搜索源
        searchRequest.source(searchSourceBuilder);
        // 执行搜索
        searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
    }

    @Test
    public void testHighLightQuery() throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "spring"));
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
    }

    @After
    public void displayHits() {
        SearchHits searchHits = searchResponse.getHits();
        long totalHits = searchHits.getTotalHits();
        System.out.println("共搜索到" + totalHits + "条文档");

        SearchHit[] hits = searchHits.getHits();
        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            String id = hit.getId();
            System.out.println("id：" + id);
            String source = hit.getSourceAsString();
            System.out.println(source);

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField highlightField = highlightFields.get("name");
                Text[] fragments = highlightField.getFragments();
                System.out.println("高亮字段：" + fragments[0].toString());
            }
        }

    }
}