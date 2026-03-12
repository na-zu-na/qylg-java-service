package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("addresses")
public class Addresses {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String phone;

    private String location;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
