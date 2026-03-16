package com.cc.qylgjavaservice.dto.OrderDTO;

import lombok.Data;
import java.util.List;

@Data
public class CustomOrderCreateDTO {
    private Long productId;

    private String purpose;

    private String style;

    private String material;

    private String budgetRange;

    private String size;

    private String remark;

    private List<String> colors;
    private List<String> patterns;
    private List<String> images;
    private Long createdAt;
}