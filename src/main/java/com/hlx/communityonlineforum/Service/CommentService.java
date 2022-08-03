package com.hlx.communityonlineforum.Service;

import com.hlx.communityonlineforum.Dao.CommentMapper;
import com.hlx.communityonlineforum.Entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired private CommentMapper commentMapper;

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
}
