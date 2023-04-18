package site.koisecret.policy.search.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
@Repository
public interface PolicyTypeMapper {
    @Select("select name from policy_type")
    List<String> getPolicyType();
}
