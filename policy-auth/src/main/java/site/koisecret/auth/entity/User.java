package site.koisecret.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: User
 * @author: 燎原
 * @since: 2023/3/31 11:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String name; //昵称
    private String phone; //手机号
    private String password; //密码
    private String province; //省份
    private String city; //城市
    private String industry; //行业
    private String profession; //职业
    private int age; //年龄 0：24以下 24~55:1 55以上 2

}
