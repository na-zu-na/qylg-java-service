package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ProductReviews;

public interface ProductReviewsService {
    Result<Long> addReview(ProductReviews productReviews);
}
