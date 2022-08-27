package com.hlx.communityonlineforum.Control;

import com.hlx.communityonlineforum.Entity.DiscussPost;
import com.hlx.communityonlineforum.Entity.Page;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.DiscussPostService;
import com.hlx.communityonlineforum.Service.LikeService;
import com.hlx.communityonlineforum.Service.UserService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * 社区在线交流论坛首页功能
 */
@Controller
public class HomeController implements CommunityOnlineForumConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        // 方法调用栈,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);

        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String,Object>> discussPostMap = new ArrayList<>();
        if (discussPosts != null){
            for(DiscussPost discussPost : discussPosts){
                Map<String,Object> m = new HashMap<>();
                m.put("discussPost",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                m.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                m.put("likeCount", likeCount);

                discussPostMap.add(m);
            }
        }
        model.addAttribute("discussPostMap",discussPostMap);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
