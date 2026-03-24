package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserDetailDTO;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserListDTO;
import com.cc.qylgjavaservice.dto.userDTO.UpdateUserDTO;
import com.cc.qylgjavaservice.dto.userDTO.UserDTO;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/auth/wechat-login")
    public Result<UserDTO> wechatLogin(@RequestBody WechatLoginRequest wechatLoginRequest){
        return userService.weChatLogin(wechatLoginRequest);
    }

    @PostMapping("/api/user/profile")
    public Result<Users> updateUserInfo(@RequestBody UpdateUserDTO updateUserDTO){
        return userService.updateUserInfo(updateUserDTO.getId(),updateUserDTO.getNickName(),updateUserDTO.getAvatarUrl());
    }

    @GetMapping("/admin/users/{userId}/status")
    public Result<Object> changeUserStatus(@PathVariable Long userId){
        return userService.changeUserStatus(userId);
    }

    @GetMapping("/admin/users/{userId}/token-blacklist")
    public Result<Object> setUserBlackList(@PathVariable Long userId){
        return userService.setUserBlackList(userId);
    }

    @GetMapping("/admin/users/list")
    public Result<AdminUserListDTO> getUsersList(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status){
        return userService.getUsersList(keyword,status);
    }

    @GetMapping("/admin/users/{userId}")
    public Result<AdminUserDetailDTO> getUsersDetail(@PathVariable Long userId){
        return userService.getUsersDetail(userId);
    }
}
