package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.HomeBanner;
import com.cc.qylgjavaservice.service.HomeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Resource
    private HomeService homeService;

    @GetMapping("/image")
    public Result<List<HomeBanner>> getHomeBanner(){
        return homeService.getHomeBanner();
    }
}
