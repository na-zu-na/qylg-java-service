package com.cc.qylgjavaservice.service.impl;

import com.cc.qylgjavaservice.dto.productsDTO.ProductDocument;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.search.ProductEsRepository;
import com.cc.qylgjavaservice.mapper.ProductsMapper;
import com.cc.qylgjavaservice.service.ProductSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSyncServiceImpl implements ProductSyncService {
    @Autowired
    private ProductsMapper productsMapper;

    private final ProductEsRepository repository;

    @Override
    public void syncAll() {
        List<Products> products = productsMapper.selectList(null);
        List<ProductDocument> list = products.stream().map(this::toDoc).toList();
        repository.saveAll(list);

    }

    @Override
    public void syncOne(Products product) {
        ProductDocument doc = toDoc(product);
        repository.save(doc);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public ProductDocument toDoc(Products p) {
        ProductDocument d = new ProductDocument();

        BeanUtils.copyProperties(p, d);

        if (p.getPrice() != null) {
            d.setPrice(String.valueOf(p.getPrice().doubleValue()));
        }
        if (p.getMinPrice() != null) {
            d.setMinPrice(String.valueOf(p.getMinPrice().doubleValue()));
        }
        if (p.getMaxPrice() != null) {
            d.setMaxPrice(String.valueOf(p.getMaxPrice().doubleValue()));
        }

        d.setSuggest(new org.springframework.data.elasticsearch.core.suggest.Completion(
                new String[]{p.getTitle()}
        ));

        return d;
    }
}
