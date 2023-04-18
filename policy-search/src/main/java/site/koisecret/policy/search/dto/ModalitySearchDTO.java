package site.koisecret.policy.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by chengsecret
 * @date 2023/4/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModalitySearchDTO {
    private String[] urlList;
    private String value;
    private int size;
    private int current;
}
