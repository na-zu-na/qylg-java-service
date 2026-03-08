package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.userDTO.UserDTO;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.entity.Users;

public interface UserService {
    Result<UserDTO> weChatLogin(WechatLoginRequest wechatLoginRequest);

    Result<Users> updateUserInfo(Long id, String nickName, String avatarUrl);
}
