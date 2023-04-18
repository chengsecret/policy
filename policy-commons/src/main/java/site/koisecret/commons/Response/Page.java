package site.koisecret.commons.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/2.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Page<T> implements Serializable {
    /**
     * 总数
     */
    protected long total = 0;
    /**
     * 每页显示条数，默认 10
     */
    protected long size = 1;

    /**
     * 当前页
     */
    protected long current = 1;

    /**
     * 查询数据列表
     */
    protected List<T> records = Collections.emptyList();

    public long offset() {
        long current = this.getCurrent();
        return current <= 1L ? 0L : Math.max((current - 1L) * this.getSize(), 0L);
    }

}
