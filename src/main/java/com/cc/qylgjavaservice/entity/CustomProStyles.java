package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("custom_pro_styles")
public class CustomProStyles {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long styleId;
}
