package com.cc.qylgjavaservice.dto.AiDTO;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
}
