package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Addresses;
import com.cc.qylgjavaservice.mapper.UserAddressMapper;
import com.cc.qylgjavaservice.service.UserAddressService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, Addresses> implements UserAddressService {

    @Autowired
    private UserAddressMapper addressMapper;

    @Override
    public Result<Long> saveAddress(Addresses addresses) {
        Long currentUserId = UserContext.getCurrentUserId();
        addresses.setUserId(currentUserId);

        if (addresses.getId() != null) {
            // 先查询该地址是否存在且属于当前用户
           Addresses existing = this.getById(addresses.getId());
            if (existing == null || !existing.getUserId().equals(currentUserId)) {
                return Result.fail(500,"无权修改");
            }
        }
        else {
            LambdaQueryWrapper<Addresses> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(Addresses::getUserId, currentUserId);
            Addresses existing = addressMapper.selectOne(queryWrapper);

            //如果当前用户存在地址
            if (existing!=null){
                addressMapper.update(addresses,queryWrapper);
                return Result.success(existing.getId());
            }
        }
        this.saveOrUpdate(addresses);

        Long id = addresses.getId();

        return Result.success(id);
    }

    @Override
    public Result<Addresses> getAddress() {
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId!=null){
            Addresses addresses = addressMapper.selectOne(new LambdaQueryWrapper<Addresses>().eq(Addresses::getUserId, currentUserId));
            return Result.success(addresses);
        }
        return Result.fail(404,"没找到");
    }
}
