package site.koisecret.esoperation.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author by chengsecret
 * @date 2023/4/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordDTO implements Serializable {
    private String policyId;
    private String[] keywords;
}
