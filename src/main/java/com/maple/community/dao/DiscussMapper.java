package com.maple.community.dao;

import com.maple.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface DiscussMapper {

    List<DiscussPost> selectDiscussPost(int userId, int offset, int limit, int orderMode);

    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(Integer id);

    @Update({
            "update discuss_post set comment_count = #{commentCount} where id = #{id}"
    })
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id,int status);

    int updateScore(int id, double score);

}
