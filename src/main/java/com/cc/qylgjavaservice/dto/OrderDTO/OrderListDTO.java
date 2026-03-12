package com.cc.qylgjavaservice.dto.OrderDTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cc.qylgjavaservice.entity.OrderItems;
import com.cc.qylgjavaservice.utils.JsonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(autoResultMap = true)
public class OrderListDTO {

    private String orderNo;

    private Integer status;

    private LocalDateTime createTime;

    private BigDecimal totalPrice;

    private Integer totalCount;

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
