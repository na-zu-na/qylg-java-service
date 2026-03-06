package com.cc.qylgjavaservice.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {
    private String code;
    private String nickName;
}
