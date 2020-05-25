package com.maple.community.service;

import com.maple.community.dao.elasticsearch.DiscussPostRepository;
import com.maple.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("elasticsearchTemplate")
    private ElasticsearchOperations elasticsearchOperations;

    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current,limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        return elasticsearchOperations.queryForPage(searchQuery, DiscussPost.class,
                new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

                        SearchHits hits = response.getHits();
                        if (hits.getTotalHits() <= 0){
                            return null;
                        }
                        List<DiscussPost> list = new ArrayList<>();
                        for (SearchHit hit : hits){
                            DiscussPost post = new DiscussPost();
                            String id = getField(hit,"id");
                            String userId = getField(hit,"userId");
                            String title = getField(hit,"title");
                            String content = getField(hit,"content");
                            String type = getField(hit,"type");
                            String status = getField(hit,"status");
                            String createTime = getField(hit,"createTime");
                            String commentCount = getField(hit,"commentCount");
                            String score = getField(hit,"score");

                            post.setId(Integer.parseInt(id));
                            post.setUserId(Integer.parseInt(userId));
                            post.setTitle(title);
                            post.setContent(content);
                            post.setType(Integer.parseInt(type));
                            post.setStatus(Integer.parseInt(status));
                            post.setCreateTime(new Date(Long.parseLong(createTime)));
                            post.setCommentCount(Integer.parseInt(commentCount));
                            post.setScore(Double.parseDouble(score));

                            // 处理高亮结果
                            HighlightField titleField = hit.getHighlightFields().get("title");
                            if (titleField != null){
                                post.setTitle(titleField.getFragments()[0].toString());
                            }
                            HighlightField contentField = hit.getHighlightFields().get("content");
                            if (contentField != null){
                                post.setContent(contentField.getFragments()[0].toString());
                            }
                            list.add(post);
                        }

                        return new AggregatedPageImpl(list,pageable, hits.getTotalHits(),
                                response.getAggregations(), response.getScrollId(), hits.getMaxScore());
                    }

                    @Override
                    public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                        return null;
                    }
                });
    }

    private String getField(SearchHit hit, String field){
        if (hit != null){
            return hit.getSourceAsMap().get(field).toString();
        }
        return null;
    }
}
