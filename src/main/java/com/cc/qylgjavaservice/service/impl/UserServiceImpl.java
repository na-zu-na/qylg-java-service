package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.UserDTO;
import com.cc.qylgjavaservice.dto.WechatLoginRequest;
import com.cc.qylgjavaservice.dto.WechatSessionDTO;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.mapper.UserMapper;
import com.cc.qylgjavaservice.service.UserService;
import com.cc.qylgjavaservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,Users> implements UserService {

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${wechat.appID}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    public UserServiceImpl(RestClient restClient,ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper=objectMapper;
    }


    @Override
    public Result<UserDTO> weChatLogin(WechatLoginRequest wechatLoginRequest) {
        String code=wechatLoginRequest.getCode();
        String url = "https://api.weixin.qq.com/sns/jscode2session";

        URI uri= UriComponentsBuilder.fromUriString(url)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUri();


        //获取响应
        String responseBody = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);


        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new RuntimeException("微信服务返回空内容");
        }

        //转化为Json
        WechatSessionDTO response = objectMapper.readValue(responseBody, WechatSessionDTO.class);

        // 业务校验
        if (response.getErrCode() != null && response.getErrCode() != 0) {
            throw new RuntimeException("微信登录失败: " + response.getErrMsg());
        }

        if (response.getOpenid() == null) {
            throw new RuntimeException("获取 OpenID 失败");
        }


        String openId=response.getOpenid();

        QueryWrapper<Users> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("openid",openId);

        Users existUser=userMapper.selectOne(queryWrapper);

        Long id;
        String nickName="未命名",avatar_url="";
        Users newUser = new Users();

        try {
            if (existUser == null) {
                newUser.setOpenId(openId);
                newUser.setNickName(wechatLoginRequest.getNickName());
                this.save(newUser);
                id = newUser.getId();
            } else {
                id = existUser.getId();
                nickName=existUser.getNickName();
                avatar_url=existUser.getAvatarUrl();
            }
        } catch (DuplicateKeyException e) {
            // 再查一次
            Users user = userMapper.selectOne(
                    new QueryWrapper<Users>().eq("openid", openId)
            );
            id = user.getId();
        }

        String token=jwtUtil.generateToken(id,openId);
        UserDTO userDTO=new UserDTO(token,id,nickName,avatar_url);

        return Result.success(userDTO);
    }

    @Override
    public Result<Users> updateUserInfo(Long id, String nickName, String avatarUrl) {
        Users user=new Users();
        user.setId(id);
        user.setNickName(nickName);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);

        return  Result.success(user);
    }
}
