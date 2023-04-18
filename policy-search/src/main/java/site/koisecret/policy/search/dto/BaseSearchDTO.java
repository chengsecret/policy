package site.koisecret.policy.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

/**
 * @author by chengsecret
 * @date 2023/4/11.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BaseSearchDTO {
    int size;
    int current;
    String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseSearchDTO that = (BaseSearchDTO) o;
        return size == that.size && current == that.current && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, current, text);
    }
}
