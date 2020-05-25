package com.maple.community.controller;
import com.maple.community.entity.DiscussPost;
import com.maple.community.entity.Page;
import com.maple.community.entity.User;
import com.maple.community.service.DiscussPostService;
import com.maple.community.service.LikeService;
import com.maple.community.service.UserService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    private DiscussPostService discussPostService;
    private UserService userService;

    @Autowired
    private LikeService likeService;


    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        // 方法调用之前，Spring MVC会自动实例化Model和Page
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);

        List<DiscussPost> discussPost = discussPostService.
                findDiscussPosts(0,page.getOffset(), page.getLimit(),orderMode);
        List<Map<String, Object>> discussPostList = new ArrayList<>();
        if (discussPost != null){
            for (DiscussPost post : discussPost){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPostList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPostList);
        model.addAttribute("orderMode",orderMode);

        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "error/404";
    }

}
