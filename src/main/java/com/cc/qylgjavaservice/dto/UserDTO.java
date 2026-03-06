package com.cc.qylgjavaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private String token;
    private Long id;
    private String nickName;
    private String avatarUrl;
}
