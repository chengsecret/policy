package site.koisecret.policy.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 搜索记录表
 * @author by chengsecret
 * @date 2023/4/9.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchHistory {
    private int id;
    private int uid; //用户
    private String category; //搜索的政策类别
    private String text; //搜索内容
    private String grade; //搜索政策的级别
    private String pubAgency; //搜索的发布机构
    private String policyType; //搜索的政策种类
    private String province; // 搜索的省份
    private String startDate; //搜索的起始日期
    private String endDate; //搜索的截止日期
    private String pubNumber; //搜索的发布字号
    private int type;  //搜索类型 0智能搜索、1条件搜索、2语义搜索、3图片搜索
}
