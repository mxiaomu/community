package com.maple.community;

import com.maple.community.dao.DiscussMapper;
import com.maple.community.dao.elasticsearch.DiscussPostRepository;
import com.maple.community.entity.DiscussPost;
import com.maple.community.service.DiscussPostService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;


    @Qualifier("elasticsearchTemplate")
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussMapper.selectDiscussPostById(243));

    }

    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("华人","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        Page<DiscussPost> search = discussPostRepository.search(searchQuery);
        System.out.println(search.getTotalElements());
        System.out.println(search.getTotalPages());
        System.out.println(search.getSize());
        System.out.println(search.getNumber());
        for (DiscussPost post : search){
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("华人","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        Page<DiscussPost> page = elasticsearchOperations.queryForPage(searchQuery,
                DiscussPost.class, new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                        SearchHits hist = response.getHits();
                        if (hist.getTotalHits() < 0){
                            return null;
                        }
                        List<DiscussPost> list = new ArrayList<>();
                        for (SearchHit hit : hist){
                            DiscussPost post = new DiscussPost();
                            String id = hit.getSourceAsMap().get("id").toString();
                            post.setId(Integer.parseInt(id));

                            HighlightField title = hit.getHighlightFields().get("title");
                            if (title != null){
                                post.setTitle(title.getFragments()[0].toString());
                            }
                            HighlightField content = hit.getHighlightFields().get("content");
                            if (content != null){
                                post.setContent(content.getFragments()[0].toString());
                            }
                            list.add(post);
                        }
                        return new AggregatedPageImpl(list,pageable,hist.getTotalHits(),
                                response.getAggregations(),response.getScrollId(),hist.getMaxScore());
                    }

                    @Override
                    public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                        return null;
                    }
                });
        for(DiscussPost post: page){
            System.out.println(post.getTitle());
            System.out.println(post.getContent());
        }

    }
}

