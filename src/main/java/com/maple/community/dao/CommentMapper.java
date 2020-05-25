package com.maple.community.dao;

import com.maple.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(Integer entityType,
                                         Integer entityId, Integer offset, int limit);

    int selectCountByEntity(Integer entityType, Integer entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
