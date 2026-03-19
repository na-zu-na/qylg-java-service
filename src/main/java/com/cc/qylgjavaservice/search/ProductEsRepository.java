package com.cc.qylgjavaservice.search;

import com.cc.qylgjavaservice.dto.productsDTO.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument,Long> {
}
