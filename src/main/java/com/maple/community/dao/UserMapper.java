package com.maple.community.dao;

import com.maple.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
    /**
     * 根据id返回用户
     * @param id
     * @return
     */
    User selectById(Integer id);

    User selectByName(String userName);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(Integer id, Integer status);

    int updatePassword(Integer id, String password);

    int updateHeader(Integer id, String headerUrl);
}
