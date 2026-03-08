package com.cc.qylgjavaservice.dto.userDTO;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private Long id;
    private String nickName;
    private String avatarUrl;
}
