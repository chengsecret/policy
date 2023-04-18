package site.koisecret.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by chengsecret
 * @date 2023/3/31.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String phone;
    private String password;
    private String code;
}
