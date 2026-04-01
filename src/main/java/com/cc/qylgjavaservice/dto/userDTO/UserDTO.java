package com.cc.qylgjavaservice.dto.userDTO;

import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserDTO {
    private String token;
    private Long id;
    private String nickName;
    private String avatarUrl;
    private UserRole userRole;
}
