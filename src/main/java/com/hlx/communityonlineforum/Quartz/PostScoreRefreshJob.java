package com.hlx.communityonlineforum.Quartz;

import com.hlx.communityonlineforum.Entity.DiscussPost;
import com.hlx.communityonlineforum.Service.DiscussPostService;
import com.hlx.communityonlineforum.Service.ElasticsearchService;
import com.hlx.communityonlineforum.Service.LikeService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计算帖子分数的定时任务
 */
public class PostScoreRefreshJob implements Job, CommunityOnlineForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 牛客纪元
    private static final Date epoch;

    static {
        try{
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        }catch (ParseException e){
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations =redisTemplate.boundSetOps(redisKey);
        if(operations.size()==0){
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        while (operations.size()>0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    /**
     * 计算帖子分数，并更新到ElasticSearch和Mysql数据库当中
     * @param postId
     */
    private void refresh(int postId) {
        DiscussPost post = discussPostService.selectDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1)) + ((post.getCreateTime().getTime() - epoch.getTime()) / (24 * 3600 * 1000));
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }

}
