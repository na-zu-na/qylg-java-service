package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.AdminLoginDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.userDTO.*;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.entity.Users;

import java.util.List;

public interface UserService {
    Result<UserDTO> weChatLogin(WechatLoginRequest wechatLoginRequest);

    Result<Users> updateUserInfo(Long id, String nickName, String avatarUrl);

    Result<Object> changeUserStatus(Long userId);

    Result<Object> setUserBlackList(Long userId);

    Result<AdminUserListDTO> getUsersList(String keyword, String status);

    public Result<AdminUserDetailDTO> getUsersDetail(Long userId);

    Result<UserDTO> adminLogin(AdminLoginDTO adminLoginDTO);

    Result<List<Users>> getCharge();

    Result<AdminAccountListVO> accountsList(int page, int pageSize, Integer status, String keyword, Integer role_code);

    Result<Void> addAccount(CreateUserDTO users);

    Result<Void> resetPassword(CreateUserDTO users);

    Result<Void> deleteAccount(Long accountId);
}
