package site.koisecret.esoperation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于生成es索引和映射
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "policy", shards = 3, replicas = 1)
public class Policy implements Serializable {
    @Id
    private String policyId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String policyTitle;
    @Field(type = FieldType.Keyword)
    private String policyGrade;
    @Field(type = FieldType.Keyword)
    private String pubAgencyId;
    @Field(type = FieldType.Keyword)
    private String pubAgency;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String pubAgencyFullName;
    @Field(type = FieldType.Keyword)
    private String pubNumber;
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd")
    private Date pubTime;
    @Field(type = FieldType.Keyword)
    private String policyType;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String policyBody;
    @Field(type = FieldType.Keyword)
    private String province;
    @Field(type = FieldType.Keyword)
    private String city;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String policySource;
    @Field(type = FieldType.Keyword)
    private String updateDate;
    @Field(type = FieldType.Keyword)
    private String[] keywords;
    @Field(type = FieldType.Keyword)
    private String[] category;
    
    // getters and setters
}