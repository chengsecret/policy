package site.koisecret.esoperation.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import site.koisecret.esoperation.entity.PolicySql;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@Repository
@Mapper
public interface PolicyMapper {

    @Insert("insert into policy (policy_id, policy_title,policy_grade,pub_agency_id,pub_agency,pub_agency_full_name,pub_number,pub_time,policy_type,policy_body,province,city,policy_source,update_date,keywords,category) " +
            "VALUES(#{policyId},#{policyTitle},#{policyGrade},#{pubAgencyId},#{pubAgency},#{pubAgencyFullName},#{pubNumber}," +
            "#{pubTime},#{policyType},#{policyBody},#{province},#{city},#{policySource},#{updateDate},#{keywords},#{category})")
    boolean addPolicy(PolicySql policy);

}
