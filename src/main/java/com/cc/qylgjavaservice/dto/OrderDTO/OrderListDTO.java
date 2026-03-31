package com.cc.qylgjavaservice.dto.OrderDTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(autoResultMap = true)
public class OrderListDTO {

    private Long id;

    private String orderNo;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private BigDecimal totalPrice;

    private Integer totalCount;

    private Long formattedCreateTime;//仅限于详细页

    //仅限于订单详情页
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipped;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closed;

    private String phone;

    private String location;


    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<OrderItemDto> goods;

    @Data
    public static class OrderItemDto {

        private Long id;

        private Long goodsId;

        private String title;

        private String cover;

        private Integer count;

        private BigDecimal price;

    }
}
