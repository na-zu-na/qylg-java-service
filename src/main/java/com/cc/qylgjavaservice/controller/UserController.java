package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.Result;
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
}
