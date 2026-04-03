package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.chatDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.entity.Conversation;
import com.cc.qylgjavaservice.entity.ConversationMember;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.mapper.ChatMessageMapper;
import com.cc.qylgjavaservice.mapper.ConversationMapper;
import com.cc.qylgjavaservice.mapper.ConversationMemberMapper;
import com.cc.qylgjavaservice.mapper.UserMapper;
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

    private final TemplateEngine templateEngine;

    private static final double MAX_LOAD = 10.0;

    /**
     * 发送消息
     */
    @Transactional
    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        message.setCreatedAt(LocalDateTime.now());

        Conversation conversation = conversationMapper.selectById(message.getConversationId());

        //更新会话的最后消息
        if (conversation!=null){
            conversation.setLastMessage(message.getContent());
            conversation.setLastMessageTime(LocalDateTime.now());

            conversationMapper.updateById(conversation);
        }
        else {
            Conversation newConversation=new Conversation();
            newConversation.setType(1);
            newConversation.setLastMessage(message.getContent());
            newConversation.setLastMessageTime(LocalDateTime.now());

            conversationMapper.insert(newConversation);
            Long id = newConversation.getId();
            message.setConversationId(id);
        }

        ConversationMember conversationMember = conversationMemberMapper.selectOne(new LambdaQueryWrapper<ConversationMember>()
                                                                                    .eq(ConversationMember::getUser_id, message.getSenderId())
                                                                                    .eq(ConversationMember::getConversation_id, message.getConversationId()));

        //创建会话成员表
        if (conversationMember==null){
            Long senderId = message.getSenderId();
            Users users = userMapper.selectById(senderId);
            UserRole userRole=users.getRoleCode();

            conversationMember=new ConversationMember();
            conversationMember.setUser_id(senderId);
            conversationMember.setConversation_id(message.getConversationId());
            if (userRole==UserRole.WORKER){
                conversationMember.setRole(1);
            } else if (userRole==UserRole.ADMIN) {
                conversationMember.setRole(0);
            }
            else conversationMember.setRole(2);
            conversationMemberMapper.insert(conversationMember);
        }

        //保存消息
        chatMessageMapper.insert(message);

        message.setId(message.getId());

        return message;
    }

    @Override
    public Result<ChatSessionVO> getOrCreateSession(Long senderId,Long receiverId, Long orderId) throws Exception {
        Long currentUserId = UserContext.getCurrentUserId();
        Long conversationId = null;

        //分配客服
        if (receiverId==null){
            Long l = allocateCustomerService(senderId);
            if (l!=null){
                receiverId=l;
            }
        } else {
            String lua="local res=redis.call('ZSCORE',KEYS[1],ARGV[1]);" +
                    "if not res then return 1 end;" +
                    "if tonumber(res) >= 10 then return 1 end;" +
                    "redis.call('ZINCRBY',KEYS[1],1,ARGV[1])" +
                    "redis.call('HSET',KEYS[2],ARGV[2],ARGV[1])" +
                    "return 0";

            Object[] value={receiverId,senderId};

            List<Object> list=new ArrayList<>();
            list.add(CS_QUEUE_KEY);
            list.add(CUSTOMER_SERVICE);

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
            if (code == 1) {
                conversationId = findExistingConversationId(senderId, receiverId);

                Long l=allocateCustomerService(senderId);
                if (l!=null){
                    receiverId=l;
                    initConversationMembers(conversationId, senderId, receiverId);

                    Conversation conv = new Conversation();
                    conv.setId(conversationId);
                    conv.setCurrentCsId(receiverId); // 显式更新当前客服为 C
                    conv.setLastMessageTime(LocalDateTime.now());
                    conversationMapper.updateById(conv);
                }
                else {
                    return Result.fail(503,"暂无可用客服");
                }
            }
        }

        if (conversationId==null){
            conversationId = findExistingConversationId(senderId, receiverId);
        }


        if (conversationId == null) {
            // 如果不存在，创建新会话
            Conversation newConversation = new Conversation();
            newConversation.setType(1); // 1: 私聊
            if (orderId != null) {
                newConversation.setOrderId(orderId);
            }
            if (receiverId!=null){
                newConversation.setCurrentCsId(receiverId);
            }
            newConversation.setLastMessageTime(LocalDateTime.now());
            // 新会话暂无最后消息内容，可留空或设为"会话已创建"

            conversationMapper.insert(newConversation);
            conversationId = newConversation.getId();

            // 3. 初始化会话成员 (必须插入两条记录：发送者和接收者)
            initConversationMembers(conversationId, senderId, receiverId);
        } else {
            // 4. 如果会话已存在，可选：更新 last_message_time 表示“重新激活”
            Conversation existingConv = conversationMapper.selectById(conversationId);
            if (existingConv != null) {
                existingConv.setLastMessageTime(LocalDateTime.now());
                conversationMapper.updateById(existingConv);
            }
        }

        // 5. 获取历史消息 (复用你已有的 getHistory 方法)
        // lastId 为 null 表示获取最新的 20 条
        List<ChatMessage> historyMessages = getHistory(conversationId, null);

        // 6. 组装返回对象
        ChatSessionVO vo = new ChatSessionVO();
        vo.setConversationId(String.valueOf(conversationId));
        vo.setMessages(historyMessages);
        vo.setReceiverId(receiverId);

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

        // 页码、分页大小兜底
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        // 查所有会话对应的用户ID（用于统计在线/离线）
        List<Long> sessionUserIds = conversationMapper.selectSessionUserIds(keyword);
        if (sessionUserIds == null || sessionUserIds.isEmpty()) {
            return Result.success(emptySessionList(safePage, safePageSize));
        }

        // 统计在线 / 离线用户
        long onlineCount = 0L;
        long nullUserCount = 0L;
        Set<Long> onlineUserIds = new HashSet<>();
        Set<Long> offlineUserIds = new HashSet<>();

        for (Long userId : sessionUserIds) {
            if (userId == null) {
                nullUserCount++;
                continue;
            }
            if (isUserOnline(userId)) {
                onlineCount++;
                onlineUserIds.add(userId);
            } else {
                offlineUserIds.add(userId);
            }
        }

        long totalCount = sessionUserIds.size();      // 总数
        long historyCount = totalCount - onlineCount; // 离线数

        // 查分页会话列表
        Page<SessionDTO> sessionPage = conversationMapper.selectSessionPage(
                new Page<>(safePage, safePageSize),
                keyword,
                status,
                new ArrayList<>(onlineUserIds),
                new ArrayList<>(offlineUserIds),
                nullUserCount > 0
        );

        // 给每条会话标记是否在线
        List<SessionDTO> records = sessionPage.getRecords();
        for (SessionDTO record : records) {
            record.setOnline(isUserOnline(record.getUserId()));
        }

        // 封装返回数据
        ChatSessionsListVO vo = new ChatSessionsListVO();
        vo.setConversations(records);
        vo.setCurrent((long) safePage);
        vo.setSize((long) safePageSize);
        vo.setTotal(sessionPage.getTotal());

        // 统计信息
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

    //分配客服
    public Long allocateCustomerService(Long senderId) throws Exception {
        RScript script = redissonClient.getScript();

        // Lua脚本（原子执行）
        String scriptContent=
                "local res = redis.call('ZRANGE', KEYS[1], 0, 0); " +
                        "if #res == 0 then return nil end; " +
                        "local csId = res[1]; " +
                        "redis.call('HSET',KEYS[2],ARGV[1],csId)"+
                        "redis.call('ZINCRBY', KEYS[1], 1, csId); " +
                        "return csId;";

        List<Object> list=new ArrayList<>();
        list.add(CS_QUEUE_KEY);
        list.add(CUSTOMER_SERVICE);
        Object[] values = { senderId };

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
        // 先检查是否已存在，避免并发重复插入
        ConversationMember existing = conversationMemberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversation_id, conversationId)
                        .eq(ConversationMember::getUser_id, userId)
        );

        Users users = userMapper.selectById(userId);
        UserRole role=users.getRoleCode();

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

    private boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        return redissonClient.getBucket(CHAT_ONLINE_USER_KEY + userId).isExists();
    }


    //修改客服可接入状态
    public void setStaffAcceptStatus(Long staffId, boolean canAccept) {
        RMap<String, Integer> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);
        acceptMap.put(staffId.toString(), canAccept ? 1 : 0);
    }

    //修改客服可接入状态
    public boolean getStaffAcceptStatus(Long staffId) {
        RMap<String, Integer> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);
        Integer status = acceptMap.get(staffId.toString());
        // 默认没配置就是可接入
        return status == null || status == 1;
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
