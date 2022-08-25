package com.hlx.communityonlineforum;


import com.hlx.communityonlineforum.Dao.DiscussPostMapper;
import com.hlx.communityonlineforum.Dao.Elasticsearch.DiscussPostRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityOnlineForumApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;
    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussMapper.selectDiscussPosts(0, 0, 200,1));

    }
}
