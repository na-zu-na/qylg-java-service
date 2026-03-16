package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.MyReviewPageVO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.service.ReviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class ReviewController {
    @Resource
    private ReviewService reviewService;

    @GetMapping("/my")
    public Result<MyReviewPageVO> getMyReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {

        return Result.success(
                reviewService.getMyReviews(page, pageSize)
        );
    }

}