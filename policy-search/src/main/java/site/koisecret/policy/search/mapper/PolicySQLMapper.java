package site.koisecret.policy.search.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import site.koisecret.policy.search.entity.PolicySQL;

/**
 * @author by chengsecret
 * @date 2023/5/21.
 */
@Repository
public interface PolicySQLMapper {
    @Select("select * from policy where policy_id=#{policyId}")
    PolicySQL findPolicySQLByPid(@Param("policyId") String policyId);

    @Update("update policy set colection_times = #{colectionTimes} " +
            "where policy_id=#{policyId}")
    boolean updateCollectionTimes(@Param("colectionTimes")Integer colectionTimes, @Param("policyId") String policyId);

    @Update("update policy set browse_times = #{browseTimes} " +
            "where policy_id=#{policyId}")
    boolean updateBrowseTimes(@Param("browseTimes")Integer browseTimes, @Param("policyId") String policyId);
}
