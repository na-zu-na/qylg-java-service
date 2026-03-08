package com.cc.qylgjavaservice.dto.userDTO;

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
