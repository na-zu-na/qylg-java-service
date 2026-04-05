package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.cc.qylgjavaservice.dto.chatDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.*;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.mapper.*;
import com.cc.qylgjavaservice.service.ChatService;
import com.cc.qylgjavaservice.utils.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.thymeleaf.TemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private ConversationMemberMapper conversationMemberMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CustomOrderMapper customOrderMapper;

    private final TemplateEngine templateEngine;

    private static final double MAX_LOAD = 10.0;

    /**
     * 发送消息
     */
    @Transactional
    @Override
    public ChatMessage sendMessage(ChatMessage message) throws Exception {
        message.setCreatedAt(LocalDateTime.now());
        RMap<String, String> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);

        //查询是否是客服
        String i = acceptMap.get(message.getSenderId().toString());

        //确保会话存在
        Conversation conversation = ensureConversationExists(message,i);
        //确保可以占用客服
        if (i==null){
            ensureCustomerServiceOccupied(conversation, message);
        }

        conversation.setLastMessage(message.getContent());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        insertMemberIfNotExists(message.getConversationId(), message.getSenderId());
        if (message.getReceiverId() != null) {
            insertMemberIfNotExists(message.getConversationId(), message.getReceiverId());
        }

        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public Result<ChatSessionVO> getSession(Long senderId, Long receiverId, Long orderId) {
        Long conversationId = null;

        // 优先按 senderId + receiverId 查询
        if (receiverId != null) {
            conversationId = findExistingConversationId(senderId, receiverId);
        }
        //else {
            // 如果前端没传 receiverId，则查该用户最近会话
            //conversationId = conversationMapper.findLatestConversationIdBySender(senderId, orderId);
        //}

        ChatSessionVO vo = new ChatSessionVO();

        // 查不到就返回空，不创建
        if (conversationId == null) {
            vo.setConversationId(null);
            vo.setMessages(Collections.emptyList());
            vo.setReceiverId(receiverId);
            return Result.success(vo);
        }

        Conversation conversation = conversationMapper.selectById(conversationId);
        List<ChatMessage> historyMessages = getHistory(conversationId, null);
        Users users = userMapper.selectById(conversation != null ? conversation.getCurrentCsId() : receiverId);

        vo.setConversationId(String.valueOf(conversationId));
        vo.setMessages(historyMessages);
        vo.setReceiverId(conversation != null ? conversation.getCurrentCsId() : receiverId);
        vo.setTargetUserInfo(users);

        return Result.success(vo);
    }


    @Transactional
    @Override
    public Result<ChatSessionVO> createSession(Long senderId, Long receiverId, Long orderId) throws Exception {
        Long conversationId;

        // 1. 如果未指定客服，真正创建时再分配
        if (receiverId == null) {
            Long csId = allocateCustomerService(senderId);
            if (csId == null) {
                return Result.fail(503, "暂无可用客服");
            }
            receiverId = csId;
        } else {
            // 2. 如果指定客服，则这时才真正尝试占用该客服
            boolean ok = bindSpecifiedCustomerService(senderId, receiverId);
            if (!ok) {
                // 指定客服不可用，则重新分配
                Long csId = allocateCustomerService(senderId);
                if (csId == null) {
                    return Result.fail(503, "暂无可用客服");
                }
                receiverId = csId;
            }
        }

        // 3. 创建前再查一次，避免重复创建
        conversationId = findExistingConversationId(senderId, receiverId);

        if (conversationId == null) {
            Conversation newConversation = new Conversation();
            newConversation.setType(1);
            if (orderId != null) {
                newConversation.setOrderId(orderId);
            }
            newConversation.setCurrentCsId(receiverId);
            newConversation.setLastMessageTime(LocalDateTime.now());

            conversationMapper.insert(newConversation);
            conversationId = newConversation.getId();

            initConversationMembers(conversationId, senderId, receiverId);
        } else {
            // 已存在则补齐成员关系
            initConversationMembers(conversationId, senderId, receiverId);
        }

        ChatSessionVO vo = new ChatSessionVO();
        Users users = userMapper.selectById(receiverId);

        vo.setConversationId(String.valueOf(conversationId));
        vo.setMessages(Collections.emptyList());
        vo.setReceiverId(receiverId);
        vo.setTargetUserInfo(users);

        return Result.success(vo);
    }

    @Override
    public void ackMessage(Long messageId) {
        ChatMessage msg = new ChatMessage();
        msg.setId(messageId);
        msg.setStatus(2); // 已送达

        //从 Redis ZSet 移除
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(ACK_RETRY_QUEUE);
        scoredSortedSet.remove(String.valueOf(messageId));

        //清理对应的重试次数计数器
        RBucket<Object> bucket = redissonClient.getBucket(ACK_RETRY_COUNT+messageId);
        bucket.delete();

        chatMessageMapper.updateById(msg);
    }

    @Override
    public void addAckRetryTask(Long messageId, int retryCount) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(ACK_RETRY_QUEUE);
        RBucket<Object> bucket = redissonClient.getBucket(ACK_RETRY_COUNT+messageId);

        String member = String.valueOf(messageId);

        // 计算延迟时间：指数退避 5s, 10s, 20s...
        long delaySeconds = 5L * (1L << retryCount);
        long score=System.currentTimeMillis()+(delaySeconds*1000);

        boolean add = scoredSortedSet.add(score,member);

        // 同时更新/设置重试次数计数器 (用于定时任务判断是否超过最大次数)
        bucket.set(retryCount, Duration.ofMinutes(30));
    }

    @Override
    public Result<ChatSessionsListVO> getSessionsList(int page, int pageSize, Integer status, String keyword) {

        // 防止非法分页参数
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        // 根据关键字查询所有会话用户ID（用于统计 + 分组）
        List<SessionUserCsDTO> sessionUserCs = conversationMapper.selectSessionUserCs(keyword);
        if (sessionUserCs == null || sessionUserCs.isEmpty()) {
            // 没有数据直接返回空结果
            return Result.success(emptySessionList(safePage, safePageSize));
        }

        // Redis中：当前“正在服务中的用户”（用户->客服绑定关系）
        RMap<String, String> csMap = redissonClient.getMap(CUSTOMER_SERVICE);

        // 转成Long集合，作为“在线会话用户”
        Set<Long> activeUserIds = csMap.readAllKeySet().stream()
                .map(this::safeParseLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 统计数据
        long onlineCount = 0L;
        long nullUserCount = 0L;

        // 用于区分在线 / 历史会话（给SQL用）
        Set<Long> onlineConversationIds = new HashSet<>();
        Set<Long> offlineConversationIds = new HashSet<>();

        // 遍历所有会话用户，区分在线和离线
        for (SessionUserCsDTO sessionUserCsDTO : sessionUserCs) {
            Long userId=sessionUserCsDTO.getUserId();
            if (userId == null) {
                // 有些异常数据（userId为空），单独统计
                nullUserCount++;
                continue;
            }
            if (activeUserIds.contains(userId)) {
                //判断是否同时在线
                String s = csMap.get(userId.toString());
                if (Objects.equals(sessionUserCsDTO.getCurrentCsId(), safeParseLong(s)) && isUserOnline(safeParseLong(s))){
                    onlineCount++;
                    onlineConversationIds.add(sessionUserCsDTO.getId());
                } else {
                    nullUserCount++;
                    offlineConversationIds.add(sessionUserCsDTO.getId());
                }
            } else {
                offlineConversationIds.add(sessionUserCsDTO.getId());
            }
        }

        // 总会话数
        long totalCount = sessionUserCs.size();
        // 历史会话数（不在线的）
        long historyCount = totalCount - onlineCount;

        // 分页查询会话列表（带在线/离线过滤）
        Page<SessionDTO> sessionPage = conversationMapper.selectSessionPage(
                new Page<>(safePage, safePageSize),
                keyword,
                status,
                new ArrayList<>(onlineConversationIds),   // 在线会话
                new ArrayList<>(offlineConversationIds),  // 离线会话
                nullUserCount > 0                 // 是否包含空用户
        );

        List<SessionDTO> records = sessionPage.getRecords();

        // 给每条记录打上“是否在线”标记（前端用）
        for (SessionDTO record : records) {
            record.setOnline(onlineConversationIds.contains(safeParseLong(record.getId())));
        }

        // 组装返回VO
        ChatSessionsListVO vo = new ChatSessionsListVO();
        vo.setConversations(records);
        vo.setCurrent((long) safePage);
        vo.setSize((long) safePageSize);
        vo.setTotal(sessionPage.getTotal());

        // 统计信息（总数 / 在线 / 历史）
        ChatSessionsListVO.Stats stats = new ChatSessionsListVO.Stats();
        stats.setTotal(totalCount);
        stats.setOnLine(onlineCount);
        stats.setHistory(historyCount);
        vo.setStats(stats);

        return Result.success(vo);
    }

    @Override
    public void exportHtml(HttpServletResponse response, Long sessionId) {
        // 1. 查询聊天记录
        List<ChatMessage> messageList = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt)
        );

        Long currentUserId = UserContext.getCurrentUserId();

        // 2. 批量查询发送者信息，避免 N+1
        Set<Long> senderIds = messageList.stream()
                .map(ChatMessage::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Users> userMap;
        if (!senderIds.isEmpty()) {
            userMap = userMapper.selectByIds(senderIds).stream()
                    .collect(Collectors.toMap(Users::getId, Function.identity(), (a, b) -> a));
        } else {
            userMap = new HashMap<>();
        }

        // 3. 组装模板数据
        List<ChatExportVO> data = messageList.stream().map(msg -> {
            ChatExportVO vo = new ChatExportVO();

            // 是否本人发送
            vo.setSelf(Objects.equals(msg.getSenderId(), currentUserId));

            // 发送者名称
            Users user = userMap.get(msg.getSenderId());
            if (user != null){
                if (user.getNickName()!=null){
                    vo.setSenderName(user.getNickName());
                } else {
                    vo.setSenderName(user.getUserName()!=null ? user.getUserName() : user.getId().toString());
                }
            } else {
                vo.setSenderName("未知用户");
                vo.setSelf(true);
            }


            // 消息内容
            vo.setContent(formatContent(msg.getContent()));

            // 时间
            vo.setTime(formatTime(msg.getCreatedAt()));

            return vo;
        }).toList();

        // 4. Thymeleaf 渲染
        Context context = new Context();
        context.setVariable("messages", data);
        context.setVariable("sessionId", sessionId);
        context.setVariable("exportTime", java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ));

        String html = templateEngine.process("chat", context);

        // 5. 输出为下载文件
        writeHtmlToResponse(response, html, "chat_" + sessionId + ".html");
    }

    @Override
    public Result<ChatStaffVO> getStaff(int page, int pageSize, String keyword) {
        //分页查询
        Page<StaffVO> pageResult = userMapper.selectStaffPage(
                new Page<>(page, pageSize),
                keyword
        );

        List<StaffVO> staffVOS = pageResult.getRecords();

        //返回对象
        ChatStaffVO chatStaffVO = new ChatStaffVO();
        chatStaffVO.setTotal(pageResult.getTotal());
        chatStaffVO.setSize(pageResult.getSize());
        chatStaffVO.setCurrent(pageResult.getCurrent());

        //统计数据
        ChatStaffVO.Stats stats = new ChatStaffVO.Stats();

        //负载set
        RScoredSortedSet<Object> zset = redissonClient.getScoredSortedSet(CS_QUEUE_KEY);
        //可接入客服hash
        RMap<String, String> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);

        Map<Object, Double> scoreMap = zset.entryRange(0, -1)
                .stream()
                .collect(Collectors.toMap(
                        ScoredEntry::getValue,
                        ScoredEntry::getScore
                ));

        int canAccept = 0;
        int busy = 0;
        int online = 0;
        double totalLoad = 0.0;

        for (StaffVO s : staffVOS) {
            String staffId = s.getId().toString();
            Double score = scoreMap.get(staffId);

            if (score == null) {
                // 离线
                s.setOnlineStatus(0);
                s.setCurrentLoad(0.0);
                s.setCanAccept(false);
                s.setActiveSessions(0.0);
                continue;
            }

            // 在线
            s.setOnlineStatus(1);
            s.setCurrentLoad(score);
            s.setActiveSessions(score);
            online++;

            //判断是否可以接入
            String manualStatus = acceptMap.get(staffId);
            boolean manualAccept = manualStatus == null || "1".equals(manualStatus);

            if (!manualAccept) {
                // 手动暂停接入
                s.setCanAccept(false);
            } else if (score < MAX_LOAD) {
                s.setCanAccept(true);
                canAccept++;
            } else {
                s.setCanAccept(false);
                busy++;
            }

            totalLoad += score;
        }

        stats.setBusy(busy);
        stats.setOnline(online);
        stats.setCanAccept(canAccept);
        stats.setTotalLoad(totalLoad);

        chatStaffVO.setStaffVOS(staffVOS);
        chatStaffVO.setStats(stats);

        return Result.success(chatStaffVO);
    }

    @Override
    public Result<Void> setStaffAcceptStatus(Long agentId, Boolean canAccept) {
        RMap<String, String> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);

        //判断是否在线
        String i = acceptMap.get(agentId.toString());
        if (i==null){
            return Result.fail(404,"用户未上线");
        }

        String put = String.valueOf(acceptMap.put(agentId.toString(), String.valueOf(canAccept ? 1 : 0)));

        if (put.equals("1") || put.equals("0")){
            return Result.success();
        }

        return Result.fail(500,"操作失败");
    }

    @Override
    public Result<List<ChatMySessions>> getWorkBench() {
        Long currentUserId = UserContext.getCurrentUserId();
        // Redis中：当前“正在服务中的用户”（用户->客服绑定关系）
        RMap<String, String> csMap = redissonClient.getMap(CUSTOMER_SERVICE);
        // 转成Long集合，作为“在线会话用户”
        Set<Long> activeUserIds = csMap.readAllKeySet().stream()
                .map(this::safeParseLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //查基本信息
        List<ChatMySessions> chatMySessions=conversationMemberMapper.selectMySessions(currentUserId);
        if (chatMySessions.isEmpty()){
            chatMySessions=Collections.emptyList();
            return Result.success(chatMySessions);
        }

        //查订单信息
        List<Long> list = chatMySessions.stream().map(ChatMySessions::getOrderId).toList();
        List<CustomOrderListDTO> customOrders = customOrderMapper.selectByOrderIds(list);
        Map<Long,CustomOrderListDTO> map=customOrders.stream()
                .collect(Collectors.toMap(CustomOrderListDTO::getId,Function.identity()));

        for (ChatMySessions c : chatMySessions){
            //判断是否在线
            if (activeUserIds.contains(c.getUserId())){
                c.setOnlineStatus(1);
            } else {
                c.setOnlineStatus(0);
            }

            //组合订单
            if (c.getOrderId()!=null){
                c.setCustomOrderListDTO(map.get(c.getOrderId()));
            }
        }


        return Result.success(chatMySessions);
    }

    //查询历史记录
    public List<ChatMessage> getHistory(Long conversationId, Long lastId) {
        QueryWrapper<ChatMessage> wrapper=new QueryWrapper<>();

        //实现分页查询
        wrapper.eq("conversation_id", conversationId)
                .lt(lastId!=null,"id",lastId)
                .orderByAsc("created_at");

        return chatMessageMapper.selectList(wrapper);
    }

    private Long findExistingConversationId(Long senderId, Long receiverId) {
        return conversationMapper.ExistingConversationId(senderId,receiverId);
    }


    /**
     * 判断是否有会话
     */
    private Conversation ensureConversationExists(ChatMessage message, String i) {
        Long conversationId = message.getConversationId();

        if (conversationId != null) {
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation != null) {
                // 同步当前客服给 message，后面占用逻辑要用
                if (i==null){
                    message.setReceiverId(conversation.getCurrentCsId());
                }

                return conversation;
            }
        }

        // conversationId 不存在或查不到，再尝试按 senderId + receiverId 查
        Long senderId = message.getSenderId();
        Long receiverId = message.getReceiverId();

        if (receiverId != null) {
            Long existingId = findExistingConversationId(senderId, receiverId);
            if (existingId != null) {
                Conversation conversation = conversationMapper.selectById(existingId);
                message.setConversationId(existingId);
                message.setReceiverId(conversation.getCurrentCsId());
                initConversationMembers(existingId, senderId, conversation.getCurrentCsId());
                return conversation;
            }
        }

        // 真没有会话，先创建一个“空壳会话”
        Conversation newConversation = new Conversation();
        newConversation.setType(1);
        newConversation.setCurrentCsId(receiverId); // 可能先为空，后面占用时再补
        newConversation.setLastMessageTime(LocalDateTime.now());

        conversationMapper.insert(newConversation);

        Long newConversationId = newConversation.getId();
        message.setConversationId(newConversationId);

        if (senderId != null) {
            insertMemberIfNotExists(newConversationId, senderId);
        }
        if (receiverId != null) {
            insertMemberIfNotExists(newConversationId, receiverId);
        }

        return newConversation;
    }

    /**
     * 判断是客服是否被占用
     */
    private void ensureCustomerServiceOccupied(Conversation conversation, ChatMessage message) throws Exception {
        Long senderId = message.getSenderId();
        Long currentCsId = conversation.getCurrentCsId();

        RMap<String, String> csMap = redissonClient.getMap(CUSTOMER_SERVICE);
        String boundCs = csMap.get(String.valueOf(senderId));

        // 1. 已经绑定到当前会话客服，且客服在线，则认为已占用
        if (boundCs != null && currentCsId != null && boundCs.equals(String.valueOf(currentCsId)) && isUserOnline(currentCsId)) {
            message.setReceiverId(currentCsId);
            return;
        }

        Long finalCsId = currentCsId;

        // 2. 会话里有客服，优先尝试重新占用这个客服
        if (finalCsId != null) {
            boolean ok = bindSpecifiedCustomerService(senderId, finalCsId);
            if (!ok) {
                finalCsId = null;
            }
        }

        // 3. 原客服不可用，则重新分配
        if (finalCsId == null) {
            finalCsId = allocateCustomerService(senderId);
            if (finalCsId == null) {
                throw new RuntimeException("暂无可用客服");
            }
        }

        // 4. 如果客服发生变化，要更新会话
        if (!Objects.equals(conversation.getCurrentCsId(), finalCsId)) {
            conversation.setCurrentCsId(finalCsId);
            conversationMapper.updateById(conversation);
        }

        // 5. 同步 message 和成员关系
        message.setReceiverId(finalCsId);
        insertMemberIfNotExists(conversation.getId(), senderId);
        insertMemberIfNotExists(conversation.getId(), finalCsId);
    }

    /**
     * 指定客服时，真正创建会话才去占用该客服
     */
    private boolean bindSpecifiedCustomerService(Long senderId, Long receiverId) {
        String lua =
                "local res=redis.call('ZSCORE',KEYS[1],ARGV[1]);" +
                        "if not res then return 1 end;" +
                        "local manualStatus = redis.call('HGET', KEYS[3], ARGV[1]);" +
                        "if manualStatus == '0' then return 1 end;" +
                        "if tonumber(res) >= tonumber(ARGV[3]) then return 1 end;" +
                        "redis.call('ZINCRBY',KEYS[1],1,ARGV[1]);" +
                        "redis.call('HSET',KEYS[2],ARGV[2],ARGV[1]);" +
                        "return 0";

        Object[] value = { receiverId, senderId, MAX_LOAD };

        List<Object> list = new ArrayList<>();
        list.add(CS_QUEUE_KEY);
        list.add(CUSTOMER_SERVICE);
        list.add(CS_ACCEPT_STATUS_KEY);

        RScript script = redissonClient.getScript();
        Object result = script.eval(
                RScript.Mode.READ_WRITE,
                lua,
                RScript.ReturnType.VALUE,
                list,
                value
        );

        int code = 0;
        if (result instanceof Integer) {
            code = (Integer) result;
        } else if (result instanceof Long) {
            code = ((Long) result).intValue();
        }

        return code == 0;
    }

    //分配客服
    public Long allocateCustomerService(Long senderId) throws Exception {
        RScript script = redissonClient.getScript();

        // Lua脚本（原子执行）
        String scriptContent=
                "local candidates = redis.call('ZRANGE', KEYS[1], 0, -1); " +
                        "if #candidates == 0 then return nil end; " +
                        "for i = 1, #candidates do " +
                        "local csId = candidates[i]; " +
                        "local load = redis.call('ZSCORE', KEYS[1], csId); " +
                        "local manualStatus = redis.call('HGET', KEYS[3], csId); " +
                        "if load and tonumber(load) < tonumber(ARGV[2]) and manualStatus ~= '0' then " +
                        "redis.call('HSET', KEYS[2], ARGV[1], csId); " +
                        "redis.call('ZINCRBY', KEYS[1], 1, csId); " +
                        "return csId; " +
                        "end; " +
                        "end; " +
                        "return nil;";

        List<Object> list=new ArrayList<>();
        list.add(CS_QUEUE_KEY);
        list.add(CUSTOMER_SERVICE);
        list.add(CS_ACCEPT_STATUS_KEY);
        Object[] values = { senderId, MAX_LOAD };

        // 4. 执行脚本
        Object result = script.eval(
                RScript.Mode.READ_WRITE,
                scriptContent,
                RScript.ReturnType.VALUE,
                list,
                values
        );

        if (result == null) {
            return null;
        }

        return Long.valueOf(result.toString());

        /**
         * 会出现并发问题：“选人 + 增加计数”必须是一个原子操作
         */
//        RMap<Object, Object> map = redissonClient.getMap(CLIENT_SERVICE_COUNT);
//        RSet<Object> set = redissonClient.getSet(CLIENT_SERVICE_COUNT);
//
//        if (map.isEmpty() && set.isEmpty()){
//            throw new Exception("当前暂无在线客服，请稍后再试");
//        }
//
//        int count=Integer.MAX_VALUE;
//
//        //找到人数最少的客服
//        Optional<Long> id = map.entrySet().stream().filter(entry -> {
//            Object valObj = entry.getValue();
//            return valObj instanceof Integer && (Integer) valObj < count;
//        }).map(entry -> {
//            //将key转换为long
//            Object keyObj = entry.getKey();
//            if (keyObj instanceof Long) {
//                return (Long) keyObj;
//            } else if (keyObj instanceof Integer) {
//                return ((Integer) keyObj).longValue();
//            }
//            return null;
//        }).filter(Objects::nonNull).findFirst();
//
//        if (id.isEmpty()){
//            throw new Exception("所有客服均忙碌，请排队等候");
//        }
//
//        map.addAndGet(id,1);
//
//        return id.get();
    }

    /**
     * 辅助方法：初始化会话成员
     * 复用你 sendMessage 中的角色逻辑
     */
    private void initConversationMembers(Long conversationId, Long senderId, Long receiverId) {
        insertMemberIfNotExists(conversationId, senderId);
        insertMemberIfNotExists(conversationId, receiverId);
    }

    private void insertMemberIfNotExists(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return;
        }

        // 先检查是否已存在，避免并发重复插入
        ConversationMember existing = conversationMemberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversation_id, conversationId)
                        .eq(ConversationMember::getUser_id, userId)
        );

        Users users = userMapper.selectById(userId);

        //默认分配为user
        UserRole role=UserRole.USER;
        if (users!=null){
            role=users.getRoleCode();
        }


        if (existing == null) {
            ConversationMember member = new ConversationMember();
            member.setConversation_id(conversationId);
            member.setUser_id(userId);
            member.setUnread_count(0);

            // 复用你的角色判断逻辑
            if (role==UserRole.WORKER) {
                member.setRole(1); // 假设 1 是商家
            } else if (role==UserRole.ADMIN) {
                member.setRole(0); // 假设 3 是客服
            } else {
                member.setRole(2); // 默认角色
            }

            conversationMemberMapper.insert(member);
        }
    }

    private ChatSessionsListVO emptySessionList(int page, int pageSize) {
        ChatSessionsListVO vo = new ChatSessionsListVO();
        vo.setConversations(new ArrayList<>());
        vo.setCurrent((long) Math.max(page, 1));
        vo.setSize((long) Math.max(pageSize, 1));
        vo.setTotal(0L);

        ChatSessionsListVO.Stats stats = new ChatSessionsListVO.Stats();
        stats.setTotal(0L);
        stats.setOnLine(0L);
        stats.setHistory(0L);
        vo.setStats(stats);
        return vo;
    }

    private Long safeParseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        return redissonClient.getBucket(CHAT_ONLINE_USER_KEY + userId).isExists();
    }

    /**
     * 将 HTML 写入响应，触发浏览器下载
     */
    private void writeHtmlToResponse(HttpServletResponse response, String html, String fileName) {
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName);
            response.getWriter().write(html);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("导出HTML失败", e);
        }
    }

    /**
     * 消息内容格式化
     * 如果你内容里可能带 HTML 标签，建议做转义，防止标签直接渲染
     */
    private String formatContent(String content) {
        if (content == null) {
            return "";
        }
        return escapeHtml(content).replace("\n", "<br/>");
    }

    /**
     * 时间格式化
     */
    private String formatTime(java.time.LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 基础 HTML 转义，防止消息内容中的标签被浏览器当成 HTML 解析
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
