package com.cc.qylgjavaservice.dto.AiDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiSearchProductDTO {
        private String type;
        private String keyword;
        private String title;
        private String purpose;
        private BigDecimal price_min;
        private BigDecimal price_max;
        private String size_range;
        private Integer make_time;
        private String sort_by;
        private Integer status;
}
