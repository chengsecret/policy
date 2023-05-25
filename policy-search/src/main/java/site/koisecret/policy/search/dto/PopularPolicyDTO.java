package site.koisecret.policy.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author by chengsecret
 * @date 2023/5/21.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularPolicyDTO implements Serializable {
    private String policyId;  //政策ID
    private String category;  // 类别
    private Integer colectionTimes; //收藏数
    private Integer browseTimes; //浏览数
}
