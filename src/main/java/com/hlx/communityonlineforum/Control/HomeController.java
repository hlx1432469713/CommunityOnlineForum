package com.hlx.communityonlineforum.Control;

import com.hlx.communityonlineforum.Entity.DiscussPost;
import com.hlx.communityonlineforum.Entity.Page;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.DiscussPostService;
import com.hlx.communityonlineforum.Service.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区在线交流论坛首页功能
 */
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // 方法调用栈,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),0);
        List<Map<String,Object>> discussPostMap = new ArrayList<>();
        if (discussPosts != null){
            for(DiscussPost discussPost : discussPosts){
                Map<String,Object> m = new HashMap<>();
                m.put("discussPost",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                m.put("user",user);
                discussPostMap.add(m);
            }
        }
        model.addAttribute("discussPostMap",discussPostMap);
        return "/index";
    }
}
