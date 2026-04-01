package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.AdminLoginDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.userDTO.*;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.service.UserService;
import jakarta.annotation.Resource;
import lombok.Data;
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

    @PostMapping("/admin/login")
    public Result<UserDTO> adminLogin(@RequestBody AdminLoginDTO adminLoginDTO){
        return userService.adminLogin(adminLoginDTO);
    }

    @GetMapping("/admin/system/accounts")
    public Result<AdminAccountListVO> accountsList(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                   @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer role_code ){
        return userService.accountsList(page,pageSize,status,keyword,role_code);
    }

    @PatchMapping("/admin/system/accounts/{accountId}/status")
    public Result<Object> changeStatus(@PathVariable Long accountId){
       return userService.changeUserStatus(accountId);
    }

    @PostMapping("/admin/system/accounts")
    public Result<Void> addAccount(@RequestBody CreateUserDTO users){
        return userService.addAccount(users);
    }

    @PostMapping("/admin/system/accounts/reset-password")
    public Result<Void> resetPassword(@RequestBody CreateUserDTO users){
        return userService.resetPassword(users);
    }

    @DeleteMapping("/admin/system/accounts/{accountId}")
    public Result<Void> deleteAccount(@PathVariable Long accountId){
        return userService.deleteAccount(accountId);
    }
}
