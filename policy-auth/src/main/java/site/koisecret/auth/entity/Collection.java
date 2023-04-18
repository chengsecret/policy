package site.koisecret.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private int id;
    private int uid; //user id
    private String pid; //政策 id 在es
}
