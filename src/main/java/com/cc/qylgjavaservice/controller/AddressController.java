package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.AddressSaveDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Addresses;
import com.cc.qylgjavaservice.service.UserAddressService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/address")
public class AddressController {
    @Resource
    private UserAddressService addressService;

    @PostMapping("/save")
    public Result<Long> saveAddress(@RequestBody Addresses addresses) {
        return addressService.saveAddress(addresses);
    }

    @GetMapping("/get")
    public Result<Addresses> getAddress() {
        return addressService.getAddress();
    }
}
