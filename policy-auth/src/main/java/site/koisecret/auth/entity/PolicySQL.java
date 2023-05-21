package site.koisecret.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PolicySQL implements Serializable {
    private int id;
    private String policyId;  //政策ID
    private String policyTitle;  //政策标题
    private String policyGrade;  //级别
    private String pubAgencyId;  //发布机构-id
    private String pubAgency;  //发布机构
    private String pubAgencyFullName;  //发布机构标准名称（全称）
    private String pubNumber;  //发布字号
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date pubTime;  //发布时间
    private String policyType;  //政策种类
    private String policyBody;  //正文内容
    private String province;  //省份
    private String city;  //地市
    private String policySource; //来源
    private String updateDate;
    private String keywords; // 提取的关键词
    private String category;  // 类别
    private Integer colectionTimes; //收藏数
    private Integer browseTimes; //浏览数
}