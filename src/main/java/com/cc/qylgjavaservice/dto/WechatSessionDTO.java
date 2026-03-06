package com.cc.qylgjavaservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatSessionDTO {
    @JsonProperty("openid")
    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;
}
