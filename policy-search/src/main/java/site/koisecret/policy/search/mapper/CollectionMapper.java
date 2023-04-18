package site.koisecret.policy.search.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@Repository
public interface CollectionMapper {
    @Select("select pid from collection where uid=#{uid}")
    List<String> findPidsByUid(int uid);

}
