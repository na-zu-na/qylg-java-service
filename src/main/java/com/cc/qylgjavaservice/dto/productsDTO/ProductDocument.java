package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Document(indexName = "products_index")
@Data
public class ProductDocument {

    @Id
    private Long id;

    private String type;

    private String title;

    private String cover;

    private String anchor;

    private String purpose;

    private String price;

    private String minPrice;

    private String maxPrice;

    private Integer totalSales;

    private Integer status;

    @CompletionField
    private Completion suggest;
}