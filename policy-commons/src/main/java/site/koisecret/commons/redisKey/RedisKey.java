package site.koisecret.commons.redisKey;

public class RedisKey {
    public static final String POLICY_CODE = "policy_code"; //存放验证码
    public static final String POLICY_CODE_TIMES = "policy_code_times"; //当天使用次数

    public static final String USER_COLLECTION = "user_collections";  //用户收藏的pids

    public static final String USER_BEHAVIOR_PID = "user_behavior_pid";  //用户浏览记录

    public static final String INDUSTRY = "industry"; //行业

    public static final String PROFESSION = "profession"; //职业

    public static final String PROVINCE = "province"; //所有省份

    public static final String CITY_OF_PROVINCE = "city_of_"; //城市

    public static final String POLICY_CATEGORY = "policy_category"; //政策类型

    public static final String POLICY_GRADE = "policy_grade"; //政策级别

    public static final String POLICY_TYPE = "policy_type"; //政策种类

    public static final String POLICY_AGENCY = "policy_agency"; //发布机构

    public static final String POLICY_POPULAR = "policy_popular"; //政策流行度排序

}