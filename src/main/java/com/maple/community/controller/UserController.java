package com.maple.community.controller;

import com.maple.community.annotation.LoginRequired;
import com.maple.community.entity.User;
import com.maple.community.service.FollowService;
import com.maple.community.service.LikeService;
import com.maple.community.service.UserService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.CommunityUtil;
import com.maple.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


@RequestMapping("user")
@Controller
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String access;

    @Value("${qiniu.key.secret}")
    private String secret;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;



    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model){
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        // 上传凭证
        Auth auth = Auth.create(access,secret);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600, policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    // 更新头像的路径
    @PostMapping("/header/url")
    @ResponseBody
    public String uploadHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空");
        }
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeaderUrl(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model){
        User user = hostHolder.getUser();
        if (!oldPassword.equals(user.getPassword())){
            model.addAttribute("passwordMsg","密码错误");
            return "site/setting";
        }
        Map<String,String> map = userService.updatePassword(user,newPassword);
        if (map.isEmpty()){
            return "redirect:/index";
        }
        model.addAttribute("passwordMsg",map.get("passwordMsg"));
        return "site/setting";
    }

    // 废弃
    @LoginRequired
    @PostMapping("/upload")
    private String uploadHeaderUrl(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        assert originalFilename != null;
        // 后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式选择错误");
            return "/site/setting";
        }
        // 生成随机文件名
        String fileName = CommunityUtil.generateUUID()+suffix;
        // 存放的文件位置
        File file = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(file);
        }catch (IOException e){
            logger.error("文件上传失败");
            throw new RuntimeException("上传文件失败， 服务器发生异常");
        }
        // 更新当前用户头像的路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);

        return "redirect:/index";
    }

    // 废弃
    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String fileName, HttpServletResponse response){
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/"+suffix);

        try (
            FileInputStream fileInputStream = new FileInputStream(fileName);
            OutputStream outputStream = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, b);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已经关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollow(hostHolder.getUser().getId(), ENTITY_TYPE_USER,
                    userId);

        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }





}
