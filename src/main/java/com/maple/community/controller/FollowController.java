package com.maple.community.controller;

import com.maple.community.entity.Event;
import com.maple.community.entity.Page;
import com.maple.community.entity.User;
import com.maple.community.event.EventProducer;
import com.maple.community.service.FollowService;
import com.maple.community.service.UserService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.CommunityUtil;
import com.maple.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"已关注");
    }


    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已经取消关注");
    }

    @GetMapping("/followees/{userId}")
    public String getFollows(@PathVariable int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setPath("/followees/"+userId);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        List<Map<String,Object>> followeeList =
                followService.findFollowees(userId,page.getOffset(), page.getLimit());
        if (followeeList != null){
            for(Map<String,Object> map : followeeList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followeeList",followeeList);
        return "site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null)
            throw new IllegalArgumentException("用户不存在");
        model.addAttribute("user",user);
        page.setRows((int) followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,
                userId));
        page.setPath("/followers/"+userId);
        page.setLimit(5);
        List<Map<String,Object>> followerList = followService.findFollowers(userId,
                page.getOffset(),page.getLimit());
        if (followerList != null){
            for (Map<String,Object> map : followerList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followerList",followerList);

        return "site/follower";
    }

    public boolean hasFollowed(int targetId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollow(hostHolder.getUser().getId(),
                CommunityConstant.ENTITY_TYPE_USER,targetId);

    }




}
