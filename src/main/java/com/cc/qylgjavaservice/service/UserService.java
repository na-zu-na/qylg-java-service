package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserDetailDTO;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserListDTO;
import com.cc.qylgjavaservice.dto.userDTO.UserDTO;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.entity.Users;
import org.springframework.web.bind.annotation.PathVariable;

public interface UserService {
    Result<UserDTO> weChatLogin(WechatLoginRequest wechatLoginRequest);

    Result<Users> updateUserInfo(Long id, String nickName, String avatarUrl);

    Result<Object> changeUserStatus(Long userId);

    Result<Object> setUserBlackList(Long userId);

    Result<AdminUserListDTO> getUsersList(String keyword, String status);

    public Result<AdminUserDetailDTO> getUsersDetail(Long userId);
}
