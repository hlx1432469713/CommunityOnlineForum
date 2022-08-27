package com.hlx.communityonlineforum.Control;

import com.hlx.communityonlineforum.Entity.Event;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Event.EventProducer;
import com.hlx.communityonlineforum.Service.LikeService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.CommunityUtil;
import com.hlx.communityonlineforum.Until.HostHolder;
import com.hlx.communityonlineforum.Until.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityOnlineForumConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId,int postId){
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        if(entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        return CommunityUtil.getJSONString(200, "点赞成功！", map);
    }
}
