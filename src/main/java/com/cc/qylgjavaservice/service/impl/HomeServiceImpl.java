package com.cc.qylgjavaservice.service.impl;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.HomeBanner;
import com.cc.qylgjavaservice.mapper.HomeMapper;
import com.cc.qylgjavaservice.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeServiceImpl implements HomeService{
    @Autowired
    private HomeMapper homeMapper;

    @Override
    public Result<List<HomeBanner>> getHomeBanner() {
        List<HomeBanner> homeBanners = homeMapper.selectList(null);

        return Result.success(homeBanners);
    }
}
