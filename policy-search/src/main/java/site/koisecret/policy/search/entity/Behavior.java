package site.koisecret.policy.search.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;


/**
 * @author by chengsecret
 * @date 2023/4/6.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Behavior {
    private int id;  //行为记录表
    private int uid; //用户
    private String pid; //政策
    private int bid; // 类型 1浏览 2收藏 3取消收藏
    private int times; //点进次数
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtModified; // 修改时间
}
