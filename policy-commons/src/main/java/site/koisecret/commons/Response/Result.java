package site.koisecret.commons.Response;

/**
 * @author by chengsecret
 * @date 2023/3/30.
 */

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Data;

import java.util.HashMap;

/**
 * 统一响应结果集
 */
@Data
public class Result {

    //操作代码
    Integer code;

    //提示信息
    String message;

    //结果数据
    HashMap<String,Object> data = new HashMap<>();

    public Result() {
    }

    public Result(String message, Integer code) {
        this.code = code;
        this.message = message;
    }

    public Result(ResultCode resultCode) {
        this.code = resultCode.code();
        this.message = resultCode.message();
    }

    public Result(ResultCode resultCode, HashMap<String,Object> data) {
        this.code = resultCode.code();
        this.message = resultCode.message();
        this.data = data;
    }

    public Result(String message) {
        this.message = message;
    }

    public static Result SUCCESS() {
        return new Result(ResultCode.SUCCESS);
    }


    public static Result FAIL() {
        return new Result(ResultCode.FAIL);
    }

    public static Result FAIL(String message) {
        return Result.FAIL(message, -1);
    }

    public static Result FAIL(String message, Integer code) {
        return new Result(message,code);
    }

    public  Result set(String key, Object value) {
        if (StringUtils.isNotEmpty(key)) {
            this.data.put(key,value);
            return this;
        }else {
            return FAIL("key不能为空");
        }
    }

}
