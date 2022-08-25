package com.hlx.communityonlineforum.Dao.Elasticsearch;

import com.hlx.communityonlineforum.Entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
