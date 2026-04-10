package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.HomeBanner;

import java.util.List;

public interface HomeService {
    Result<List<HomeBanner>> getHomeBanner();
}
