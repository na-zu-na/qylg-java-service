package com.cc.qylgjavaservice.dto.OrderDTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomOrderListDTO {
    private Long id;
    private Long productId;
    private String title;
    private String orderNo;
    private String purpose;

    private String style;

    private String material;

    private BigDecimal minBudget;

    private BigDecimal maxBudget;

    private String budget;

    private String size;

    private String remark;

    private Integer status;

    private String statusText;

    private int isConfirmed;

    private BigDecimal quote;

    private String proposalImage;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> colorOptions;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> patternOptions;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> images;
    private Long createdAt;
}
