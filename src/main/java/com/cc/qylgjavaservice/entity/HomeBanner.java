package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("home_banners")
@Data
public class HomeBanner {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String image;
}
