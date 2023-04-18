package site.koisecret.esoperation.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import site.koisecret.esoperation.entity.Policy;

@Repository
public interface PolicyDao extends ElasticsearchRepository<Policy, String> {

}