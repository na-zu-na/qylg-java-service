package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Addresses;

public interface UserAddressService {
    Result<Long> saveAddress(Addresses addresses);

    Result<Addresses> getAddress();
}
