package site.koisecret.esoperation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PolicySql {
    private String policyId;
    private String policyTitle;
    private String policyGrade;
    private String pubAgencyId;
    private String pubAgency;
    private String pubAgencyFullName;
    private String pubNumber;
    private String pubTime;
    private String policyType;
    private String policyBody;
    private String province;
    private String city;
    private String policySource;
    private String updateDate;
    private String keywords;
    private String category;
}
