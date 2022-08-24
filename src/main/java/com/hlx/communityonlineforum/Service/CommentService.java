package com.hlx.communityonlineforum.Service;

import com.hlx.communityonlineforum.Dao.CommentMapper;
import com.hlx.communityonlineforum.Entity.Comment;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityOnlineForumConstant {

    @Autowired private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 寻找帖子的评论和回复
     * @param entityType ： 类别（1 ： 帖子的评论 2 ：评论的回复）
     * @param entityId ： 对于帖子的id 和 评论的id
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 新增评论 + 事务机制
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null)
            throw new IllegalArgumentException("评论具体参数不能为空!");
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//过滤敏感词
        int rows = commentMapper.insertComment(comment);

        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //找，对于帖子的评论，且是指定帖子Id
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
