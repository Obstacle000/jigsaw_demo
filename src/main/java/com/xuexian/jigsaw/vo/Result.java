package com.xuexian.jigsaw.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 后端统一返回结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "通用返回结果")
public class Result<T> implements Serializable {

    @Schema(description = "状态码")
    private Integer code; // 编码：1成功，0和其它数字为失败

    @Schema(description = "信息")
    private String msg; // 错误信息

    @Schema(description = "数据")
    private T data; // 数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(Integer code, T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = code;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = code;
        return result;
    }
}
