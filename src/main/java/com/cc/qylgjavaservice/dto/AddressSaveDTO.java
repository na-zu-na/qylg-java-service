package com.cc.qylgjavaservice.dto;

import lombok.Data;

@Data
public class AddressSaveDTO {
    private String name;

    private String phone;

    private String location;

    // 可选：如果前端传递了 ID，则代表更新；否则代表新增
    private Long addressId;
}