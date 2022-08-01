package com.hlx.communityonlineforum.Control;

import com.hlx.communityonlineforum.Entity.DiscussPost;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.DiscussPostService;
import com.hlx.communityonlineforum.Service.UserService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.CommunityUtil;
import com.hlx.communityonlineforum.Until.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityOnlineForumConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 新增帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(value = "/addDiscussPost", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录哦!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);

        return CommunityUtil.getJSONString(200,"恭喜你，帖子发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}" , method = RequestMethod.GET)
    public String detail(@PathVariable("discussPostId") int discussPostId, Model model){
        // 帖子
        DiscussPost discussPost = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        // 作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        return "/site/discuss-detail";

    }
}
