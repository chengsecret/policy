package site.koisecret.auth.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/7.
 */
@Repository
public interface CategoryMapper {

    @Select("select name from industry")
    List<String> getIndustries();

    @Select("select name from profession")
    List<String> getProfessions();

}
