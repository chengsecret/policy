package site.koisecret.policy.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author by chengsecret
 * @date 2023/4/9.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ConditionSearchDTO {
    private int current;
    private int size;
    private String text; //搜索内容
    private String[] category; //政策类别
    private String categoryWeight; //权重
    private String agency; //发布机构
    private String agencyWeight;
    private String[] province; //省份
    private String provinceWeight;
    private String pubNumber; //发布字号
    private String pubNumberWeight;
    private String[] grade; //政策级别
    private String gradeWeight;
    private String startDate; // 发布日期
    private String endDate; //发布日期
    private String dateWeight;
    private String[] type; //政策类型
    private String typeWeight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionSearchDTO that = (ConditionSearchDTO) o;
        return current == that.current && size == that.size && Objects.equals(text, that.text) && Arrays.equals(category, that.category) && Objects.equals(categoryWeight, that.categoryWeight) && Objects.equals(agency, that.agency) && Objects.equals(agencyWeight, that.agencyWeight) && Arrays.equals(province, that.province) && Objects.equals(provinceWeight, that.provinceWeight) && Objects.equals(pubNumber, that.pubNumber) && Objects.equals(pubNumberWeight, that.pubNumberWeight) && Arrays.equals(grade, that.grade) && Objects.equals(gradeWeight, that.gradeWeight) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(dateWeight, that.dateWeight) && Arrays.equals(type, that.type) && Objects.equals(typeWeight, that.typeWeight);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(Arrays.toString(category), current, size, text, categoryWeight, agency,
                agencyWeight, provinceWeight, pubNumber, pubNumberWeight, gradeWeight, Arrays.toString(province),
                Arrays.toString(grade),Arrays.toString(type),
                startDate, endDate, dateWeight, typeWeight);
        return result;
    }
}
