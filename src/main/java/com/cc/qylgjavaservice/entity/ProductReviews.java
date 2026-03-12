package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cc.qylgjavaservice.utils.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Data
@TableName("product_reviews")
public class ProductReviews {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long orderId;

    private Long goodsId;

    private String content;
    private int rate;

    @TableField(typeHandler = JsonbTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> images;

    private Long createdAt;

}
