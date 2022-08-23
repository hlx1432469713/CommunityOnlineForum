package com.hlx.communityonlineforum.Control;

import com.hlx.communityonlineforum.Annotation.LoginRequired;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.FollowService;
import com.hlx.communityonlineforum.Service.LikeService;
import com.hlx.communityonlineforum.Service.UserService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.CommunityUtil;
import com.hlx.communityonlineforum.Until.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityOnlineForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${communityOnlineForum.path.upload}")
    private String uploadPath;

    @Value("${communityOnlineForum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * 返回个人信息设置页面
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeadUrl(MultipartFile headImage, Model model){
        if (headImage == null){
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }
        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/user/headUrl/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/headUrl/" + fileName;
        userService.updateHeaderUrl(user.getId(),headUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "/headUrl/{fileName}",method = RequestMethod.GET)
    public void getHeadUrl(@PathVariable("fileName")String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream outputStream = response.getOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        }catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }
    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String profile(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null)
            throw new RuntimeException("该用户不存在!");
        // 用户
        model.addAttribute("user", user);

        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
