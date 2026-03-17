package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cc.qylgjavaservice.dto.ChatSessionVO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.entity.Conversation;
import com.cc.qylgjavaservice.entity.ConversationMember;
import com.cc.qylgjavaservice.mapper.ChatMessageMapper;
import com.cc.qylgjavaservice.mapper.ConversationMapper;
import com.cc.qylgjavaservice.mapper.ConversationMemberMapper;
import com.cc.qylgjavaservice.service.ChatService;
import com.cc.qylgjavaservice.service.UserService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.cc.qylgjavaservice.utils.RedisConstants.CS_QUEUE_KEY;
import static com.cc.qylgjavaservice.utils.RedisConstants.CUSTOMER_SERVICE;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private ConversationMemberMapper conversationMemberMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RedissonClient redissonClient;

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
            conversationMember=new ConversationMember();
            conversationMember.setUser_id(message.getSenderId());
            conversationMember.setConversation_id(message.getConversationId());
            if (message.getSenderId()>100 && message.getSenderId()<200){
                conversationMember.setRole(2);
            } else if (message.getSenderId()>200 && message.getSenderId()<300) {
                conversationMember.setRole(3);
            }
            else conversationMember.setRole(1);
            conversationMemberMapper.insert(conversationMember);
        }

        //保存消息
        chatMessageMapper.insert(message);

        return message;
    }

    @Override
    public Result<ChatSessionVO> getOrCreateSession(Long senderId,Long receiverId, Long orderId) throws Exception {
        Long currentUserId = UserContext.getCurrentUserId();

        //分配客服
        if (receiverId==null){
            Long l = allocateCustomerService(senderId);
            if (l!=null){
                receiverId=l;
            }
        } else {
            String lua="local res=redis.call('ZSCORE',KEYS[1],ARGV[1]);" +
                    "if not res then return nil end;" +
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
                return Result.fail(503, "客服繁忙，请稍后再试");
            }
        }

        Long conversationId = findExistingConversationId(senderId, receiverId);

        if (conversationId == null) {
            // 如果不存在，创建新会话
            Conversation newConversation = new Conversation();
            newConversation.setType(1); // 1: 私聊
            if (orderId != null) {
                newConversation.setOrderId(orderId);
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

    //查询历史记录
    public List<ChatMessage> getHistory(Long conversationId, Long lastId) {
        QueryWrapper<ChatMessage> wrapper=new QueryWrapper<>();

        //实现分页查询
        wrapper.eq("conversation_id", conversationId)
                .lt(lastId!=null,"id",lastId)
                .orderByAsc("created_at")
                .last("limit 20");

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
            throw new Exception("当前暂无在线客服，请稍后再试");
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

        if (existing == null) {
            ConversationMember member = new ConversationMember();
            member.setConversation_id(conversationId);
            member.setUser_id(userId);
            member.setUnread_count(0);

            // 复用你的角色判断逻辑
            if (userId > 100 && userId < 200) {
                member.setRole(2); // 假设 2 是商家
            } else if (userId > 200 && userId < 300) {
                member.setRole(3); // 假设 3 是客服
            } else {
                member.setRole(1); // 默认角色
            }

            conversationMemberMapper.insert(member);
        }
        }
}
