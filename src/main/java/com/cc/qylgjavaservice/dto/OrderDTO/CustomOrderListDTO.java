package com.cc.qylgjavaservice.dto.OrderDTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private Long workerId;

    private String workerName;

    private Long userId;

    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime proposalTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime quoteTime;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> colorOptions;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> patternOptions;

    @TableField(typeHandler = JacksonTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> images;
    private Long createdAt;
}
