package com.maple.community.util;

/**
 * 使用枚举类来代替
 */

public interface CommunityConstant {

    public int ACTIVATION_SUCCESS = 0;

    public int ACTIVATION_REPEAT = 1;

    public int ACTIVATION_FAILURE = -1;

    public int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    public int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型： 帖子
     */
    public int ENTITY_TYPE_POST=1;

    /**
     * 实体类型： 评论
     */
    public int ENTITY_TYPE_COMMENT=2;

    /**
     * 实体类型： 用户
     */
    public int ENTITY_TYPE_USER = 3;

    // 主题类型
    // 评论
    String TOPIC_COMMENT = "comment";
    // 点赞
    String TOPIC_LIKE = "like";
    // 关注
    String TOPIC_FOLLOW = "follow";
    // 分享
    String TOPIC_SHARE = "share";
    // 系统ID
    int SYSTEM_USER_ID = 1;

    /**
     * 发布帖子事件
     */
    String TOPIC_PUBLISH = "publish";

    // 删除帖子
    String TOPIC_DELETE = "delete";

    /**
     * 权限：
     *      普通用户
     *      管理员
     *      版主
     */
    String AUTHORITY_USER = "user";
    String AUTHORITY_ADMIN="admin";
    String AUTHORITY_MODERATOR = "moderator";

}
