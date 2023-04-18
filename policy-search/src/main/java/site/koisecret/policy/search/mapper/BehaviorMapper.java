package site.koisecret.policy.search.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import site.koisecret.policy.search.entity.Behavior;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/6.
 */
@Repository
public interface BehaviorMapper {
    @Select("select pid from behavior where uid=#{uid}")
    List<String> findPidsByUid(int uid);

    @Select("select * from behavior where uid=#{uid} and pid=#{pid}")
    Behavior findBehavior(@Param("uid") int uid, @Param("pid") String pid);

    boolean addBehavior(Behavior behavior);

    @Update("update behavior set bid = #{bid},times = #{times} " +
            "where id = #{id}")
    boolean updateBehavior(Behavior behavior);

}
