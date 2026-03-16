package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.MyReviewPageVO;

public interface ReviewService {
    MyReviewPageVO getMyReviews(Integer page, Integer pageSize);
}
