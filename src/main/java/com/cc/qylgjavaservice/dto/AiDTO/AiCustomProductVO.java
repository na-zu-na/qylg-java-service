package com.cc.qylgjavaservice.dto.AiDTO;

import lombok.Data;

import java.util.List;

@Data
public class AiCustomProductVO {
    private String purpose;

    /**
     * 已填写的风格偏好
     */
    private String style;

    /**
     * 已填写的材质倾向
     */
    private String material;

    /**
     * 已填写的预算区间
     */
    private String budgetRange;

    /**
     * 已填写的尺寸/规格
     */
    private String size;

    /**
     * 已填写的备注说明
     */
    private String remark;

    /**
     * 已选择的颜色
     */
    private List<String> colors;

    /**
     * 已选择的纹样
     */
    private List<String> patterns;

    /**
     * ai的解释
     */
    private String aiRemark;
}
