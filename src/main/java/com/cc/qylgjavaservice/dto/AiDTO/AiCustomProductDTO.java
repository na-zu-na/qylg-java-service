package com.cc.qylgjavaservice.dto.AiDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class AiCustomProductDTO {
    private String text;

    /**
     * 商品可选风格列表
     */
    private List<String> styleList;

    /**
     * 商品可选材质列表
     */
    private List<String> materialList;

    private List<String> purposeList;

    private List<String> budgetRangeList;

    private List<String> colorList;
    private List<String> patternList;
}
