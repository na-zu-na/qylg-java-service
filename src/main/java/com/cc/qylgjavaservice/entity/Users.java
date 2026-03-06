package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.enums.UserStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "users",autoResultMap = true) // 指定表名
public class Users {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("openid")
    private String openId;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 角色代码
     */
    @TableField("role_code")
    private UserRole roleCode = UserRole.USER;

    /**
     * 状态
     */
    @TableField("status")
    private UserStatus statusCode = UserStatus.NORMAL;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}