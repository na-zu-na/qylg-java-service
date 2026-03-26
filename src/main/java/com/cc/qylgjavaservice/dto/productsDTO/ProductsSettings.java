package com.cc.qylgjavaservice.dto.productsDTO;

import com.cc.qylgjavaservice.entity.Materials;
import com.cc.qylgjavaservice.entity.Styles;
import lombok.Data;

import java.util.List;

@Data
public class ProductsSettings {
    private List<Materials> materialList;
    private List<Styles> styleList;
}
