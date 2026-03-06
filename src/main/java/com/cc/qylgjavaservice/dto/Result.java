package com.cc.qylgjavaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    //操作成功无数据
    public static <T>Result<T> success(){
        return new Result<>(200,"success",null);
    }

    //操作成功有数据
    public static <T>Result<T> success(T data){
        return new Result<>(200,"success",data);
    }

    // 失败 (系统错误或业务错误)
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // 失败 (常用 500)
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}