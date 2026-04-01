package com.cc.qylgjavaservice.dto.userDTO;

import lombok.Data;

@Data
public class CreateUserDTO {
    private Long id;
    private String userName;
    private String nickName;
    private String password;
    private String phone;
    private Integer roleCode;
}
