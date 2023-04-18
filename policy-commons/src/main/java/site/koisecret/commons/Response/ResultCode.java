package site.koisecret.commons.Response;

/**
 * @author by chengsecret
 * @date 2023/3/30.
 */
/**
 * 通用响应状态
 */
public enum ResultCode {

    /* 成功状态码 */
    SUCCESS(200, "操作成功！"),

    /* 错误状态码 */
    FAIL(-1, "操作失败！"),
    //token过期
    TOKEN_EXPIRED(402, "token expired");


    //操作代码
    int code;
    //提示信息
    String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}