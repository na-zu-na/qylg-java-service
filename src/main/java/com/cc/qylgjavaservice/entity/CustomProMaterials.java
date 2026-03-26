package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("custom_pro_materials")
public class CustomProMaterials {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productsId;

    private Long materialId;
}
