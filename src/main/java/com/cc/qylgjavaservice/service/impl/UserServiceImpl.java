package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.*;
import com.cc.qylgjavaservice.dto.userDTO.*;
import com.cc.qylgjavaservice.entity.*;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.enums.UserStatus;
import com.cc.qylgjavaservice.mapper.*;
import com.cc.qylgjavaservice.service.UserService;
import com.cc.qylgjavaservice.utils.JwtUtil;
import com.cc.qylgjavaservice.utils.UserContext;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

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
    private RedissonClient redissonClient;

    @Autowired
    private OrdersMapper orderMapper;

    @Autowired
    private CustomOrderMapper customOrderMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AddressesMapper addressMapper;

    @Autowired
    private ConversationMemberMapper conversationMemberMapper;

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
        //插入redis
        RBucket<Object> bucket = redissonClient.getBucket(USER_TOKEN_KEY + token);
        bucket.set(id.toString(),Duration.ofHours(1));
        UserDTO userDTO=new UserDTO();
        userDTO.setToken(token);
        userDTO.setId(id);
        userDTO.setNickName(nickName);
        userDTO.setAvatarUrl(avatar_url);

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

    @Override
    public Result<Object> changeUserStatus(Long userId) {
        Users users = userMapper.selectById(userId);
        if (users!=null){
            if (users.getStatusCode()==UserStatus.BANNED){
                users.setStatusCode(UserStatus.NORMAL);
            }
            else users.setStatusCode(UserStatus.BANNED);
            int i = userMapper.updateById(users);
            if (i==1){
                return Result.success();
            }
            else {
                return Result.fail(500,"无法更新");
            }
        }
        return Result.fail(500,"不存在用户");
    }

    @Override
    public Result<Object> setUserBlackList(Long userId) {
        RBucket<String> tokenBucket = redissonClient.getBucket(USER_BLACK_KEY +userId);
        if (!tokenBucket.isExists()){
            return Result.fail(500,"用户未登录");
        }
        String token = tokenBucket.get();
        RBucket<String> blackBucket = redissonClient.getBucket(BLACKLIST_PREFIX + token);
        if (!blackBucket.isExists()){
            blackBucket.set("requestDenied", Duration.ofHours(2));
            return Result.success();
        }

        return Result.fail(500,"黑名单已存在");

    }

    @Override
    public Result<AdminUserListDTO> getUsersList(String keyword, String status) {
        UserStatus s = null;
        AdminUserListDTO adminUserListDTO = new AdminUserListDTO();

        if (Objects.equals(status, "normal")) {
            s = UserStatus.NORMAL;
        } else if (Objects.equals(status, "disabled")) {
            s = UserStatus.BANNED;
        }

        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<Users>()
                .eq(Users::getRoleCode, UserRole.USER);

        // 状态筛选
        if (s != null) {
            wrapper.eq(Users::getStatusCode, s);
        }

        // 关键词筛选
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();

            wrapper.and(w -> {
                // 如果 keyword 是纯数字，可以查 id
                if (kw.matches("\\d+")) {
                    w.eq(Users::getId, Long.valueOf(kw)).or();
                }
                w.like(Users::getNickName, kw)
                        .or()
                        .like(Users::getOpenId, kw);
            });
        }

        List<Users> users = userMapper.selectList(wrapper);

        adminUserListDTO.setStats(userMapper.getUserStats());
        adminUserListDTO.setUsers(users);

        return Result.success(adminUserListDTO);
    }

    @Override
    public Result<AdminUserDetailDTO> getUsersDetail(Long userId) {
            AdminUserDetailDTO dto = new AdminUserDetailDTO();

            // 1. 用户基本信息
            Users user = userMapper.selectById(userId);
            dto.setUsers(user);

            // 2. 各种统计
            dto.setOrderCount(orderMapper.selectCount(
                    new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, userId)
            ).intValue());

            dto.setCustomOrderCount(customOrderMapper.selectCount(
                    new LambdaQueryWrapper<CustomOrder>().eq(CustomOrder::getUserId, userId)
            ).intValue());

            dto.setArticleCount(articleMapper.selectCount(
                    new LambdaQueryWrapper<Articles>().eq(Articles::getAuthorId, userId)
            ).intValue());

            // 3. 总消费
            Long totalSpend = orderMapper.sumTotalSpend(userId);
            dto.setTotalSpend(totalSpend == null ? 0 : totalSpend);

            // 4. 地址
            Addresses address = addressMapper.selectOne(
                    new LambdaQueryWrapper<Addresses>().eq(Addresses::getUserId, userId).last("limit 1")
            );
            dto.setAddresses(address);

            // 5. 订单历史
            List<Orders> orders = orderMapper.selectList(
                    new LambdaQueryWrapper<Orders>()
                            .eq(Orders::getUserId, userId)
                            .orderByDesc(Orders::getCreatedAt)
            );
            dto.setOrderHistory(orders);

            // 6. 定制订单
            List<CustomOrder> customOrders = customOrderMapper.selectList(
                    new LambdaQueryWrapper<CustomOrder>()
                            .eq(CustomOrder::getUserId, userId)
                            .orderByDesc(CustomOrder::getCreatedAt)
            );
            dto.setCustomOrders(customOrders);

            // 7. 文章
            List<Articles> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Articles>()
                            .eq(Articles::getAuthorId, userId)
                            .orderByDesc(Articles::getCreatedAt)
            );
            dto.setArticlesList(articles);

            // 8. 会话
            List<ConversationSessionsDTO> sessions = conversationMemberMapper.getConversationSessionsByUserId(userId);
            dto.setConversationMemberList(sessions);

            return Result.success(dto);
        }

    @Override
    public Result<UserDTO> adminLogin(AdminLoginDTO adminLoginDTO) {
        Users users = userMapper.selectOne(new LambdaQueryWrapper<Users>()
                .eq(Users::getUserName, adminLoginDTO.getUsername())
                .eq(Users::getPassword,adminLoginDTO.getPassword()));

        if (users!=null){
            if (users.getRoleCode()==UserRole.USER){
                return Result.fail(401,"权限不足");
            }

            String token=jwtUtil.generateToken(users.getId(), users.getOpenId());
            //加入redis
            RBucket<Object> bucket = redissonClient.getBucket(ADMIN_TOKEN_KEY+token);
            bucket.set(users.getId().toString(),Duration.ofHours(1));
            UserDTO userDTO=new UserDTO();
            userDTO.setId(users.getId());
            userDTO.setAvatarUrl(users.getAvatarUrl());
            userDTO.setNickName(users.getNickName());
            userDTO.setToken(token);
            userDTO.setUserRole(users.getRoleCode());

            return Result.success(userDTO);
        }

        return Result.fail(401, "用户名或密码错误");
    }

    @Override
    public Result<List<Users>> getCharge() {
        List<Users> users = userMapper.selectList(new LambdaQueryWrapper<Users>().eq(Users::getRoleCode, UserRole.WORKER));

        return Result.success(users);
    }

    @Override
    public Result<AdminAccountListVO> accountsList(int page, int pageSize, Integer status, String keyword, Integer role_code) {
        Page<Users> usersPage=new Page<>(page,pageSize);

        //查权限
        if (checkAuth()){
            return Result.fail(401,"权限不足");
        }

        //查列表
        Page<Users> dto = userMapper.selectAccountList(usersPage,status,keyword,role_code);

        AdminAccountListVO adminAccountListVO=new AdminAccountListVO();
        //赋值
        adminAccountListVO.setSize(dto.getSize());
        adminAccountListVO.setTotal(dto.getTotal());
        adminAccountListVO.setCurrent(dto.getCurrent());
        adminAccountListVO.setUsers(dto.getRecords());

        //查统计
        AdminAccountListVO.Stats stats=userMapper.selectAccountStats();
        adminAccountListVO.setStats(stats);

        return Result.success(adminAccountListVO);
    }

    @Override
    public Result<Void> addAccount(CreateUserDTO dto) {
        if (dto==null){
            return Result.fail(400,"请求参数错误");
        }

        //查权限
        if (checkAuth()){
            return Result.fail(401,"权限不足");
        }

        Users user = new Users();
        user.setUserName(dto.getUserName());
        user.setNickName(dto.getNickName());
        user.setPhone(dto.getPhone());
        if (dto.getPassword()!=null){
            user.setPassword(dto.getPassword());
        } else user.setPassword("123456");

        UserRole role=null;
        if (dto.getRoleCode()==0){
            role=UserRole.ADMIN;
        } else if (dto.getRoleCode()==1) {
            role=UserRole.WORKER;
        } else {
            return Result.fail(400,"参数错误");
        }

        user.setRoleCode(role);
        user.setStatusCode(UserStatus.NORMAL);

        int insert = userMapper.insert(user);
        if (insert<1){
            return Result.fail(500,"插入错误");
        }

        return Result.success();
    }

    @Override
    public Result<Void> resetPassword(CreateUserDTO dto) {
        if (dto.getId()==null){
            return Result.fail(400,"请输入id");
        }

        //查权限
        if (checkAuth()){
            return Result.fail(401,"权限不足");
        }

        Users users = userMapper.selectById(dto.getId());
        if (users==null){
            return Result.fail(404,"用户不存在");
        }

        users.setPassword(dto.getPassword());
        int i = userMapper.updateById(users);

        if (i<1){
            return Result.fail(500,"更新错误");
        }

        return Result.success();
    }

    @Override
    public Result<Void> deleteAccount(Long accountId) {
        //查权限
        if (checkAuth()){
            return Result.fail(401,"权限不足");
        }

        Users users = userMapper.selectById(accountId);
        if (users==null){
            return Result.fail(404,"用户不存在");
        }

        int i = userMapper.deleteById(accountId);
        if (i<1){
            return Result.fail(500,"更新错误");
        }

        return Result.success();
    }

    private boolean checkAuth(){
        Long currentUserId = UserContext.getCurrentUserId();
        Users users = userMapper.selectById(currentUserId);
        //查权限
        if (users!=null){
            return users.getRoleCode() == UserRole.USER;
        }

        return true;
    }
}
