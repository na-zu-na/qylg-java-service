package com.cc.qylgjavaservice.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.mapper.UserMapper;
import com.cc.qylgjavaservice.service.ChatService;
import com.cc.qylgjavaservice.service.UserService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocket {

    private static ChatService chatService;

    private static RedissonClient redissonClient;

    private static UserMapper userMapper;

    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        ChatWebSocket.redissonClient = redissonClient;
    }

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatWebSocket.chatService = chatService;
    }

    @Autowired
    public void setUserService(UserMapper userMapper){ChatWebSocket.userMapper=userMapper;}

    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {

        UserSessionManager.add(userId, session);
        markUserOnline(userId, session.getId());
        RScoredSortedSet<Long> scoredSortedSet = redissonClient.getScoredSortedSet(CS_QUEUE_KEY);
        Users users = userMapper.selectById(userId);
        UserRole roleCode = users.getRoleCode();

        if (roleCode==UserRole.USER){
            System.out.println("用户连接：" + userId);
        }
        else {
            scoredSortedSet.add(0,userId);

            //客服是否接入队列
            RMap<String, Integer> acceptMap = redissonClient.getMap(CS_ACCEPT_STATUS_KEY);
            acceptMap.putIfAbsent(userId.toString(), 1); // 默认可接入

            System.out.println("客服连接：" + userId);
        }


    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {

        UserSessionManager.remove(userId, session);
        markUserOffline(userId, session.getId());
        Users users = userMapper.selectById(userId);
        UserRole roleCode = users.getRoleCode();

        if (roleCode==UserRole.USER){
            String lua =
                    "local csId = redis.call('HGET', KEYS[2], ARGV[1]); " +
                            "if not csId then return 0 end; " +
                            "local score = redis.call('ZSCORE', KEYS[1], csId); " +
                            "if score then redis.call('ZINCRBY', KEYS[1], -1, csId); end; " +
                            "redis.call('HDEL', KEYS[2], ARGV[1]); " +
                            "return 1;";

            List<Object> keys = new ArrayList<>();
            keys.add(CS_QUEUE_KEY);      // KEYS[1]: ZSet (负载队列)
            keys.add(CUSTOMER_SERVICE);  // KEYS[2]: Hash (用户->客服映射)

            Object[] values = { userId }; // ARGV[1]: 用户 ID

            try {
                RScript script = redissonClient.getScript();
                script.eval(
                        RScript.Mode.READ_WRITE,
                        lua,
                        RScript.ReturnType.VALUE,
                        keys,
                        values
                );
                System.out.println("用户离线，已释放客服负载：" + userId);
            } catch (Exception e) {
                System.err.println("用户离线处理失败：" + e.getMessage());
            }

            System.out.println("user offline: " + userId);
        }
        else {
            String lua =
                    "redis.call('ZREM', KEYS[1], ARGV[1]); " +
                            "local all = redis.call('HGETALL', KEYS[2]); " +
                            "for i = 1, #all, 2 do " +
                            "local userKey = all[i]; " +
                            "local csId = all[i + 1]; " +
                            "if csId == ARGV[1] then redis.call('HDEL', KEYS[2], userKey); end; " +
                            "end; " +
                            "return 1;";

            List<Object> keys = new ArrayList<>();
            keys.add(CS_QUEUE_KEY);
            keys.add(CUSTOMER_SERVICE);

            Object[] values = { String.valueOf(userId) };

            try {
                RScript script = redissonClient.getScript();
                script.eval(
                        RScript.Mode.READ_WRITE,
                        lua,
                        RScript.ReturnType.VALUE,
                        keys,
                        values
                );
            } catch (Exception e) {
                System.err.println("客服离线处理失败：" + e.getMessage());
            }
            System.out.println("客服离线：" + userId);
        }
    }

    /**
     * 接收消息
     */
    @OnMessage
    public void onMessage(String messageJson, Session session) throws Exception {

        JSONObject obj = JSON.parseObject(messageJson);
        String type = obj.getString("type");

        if (Objects.equals(type, "CHAT")){
            ChatMessage message = obj.getObject("data", ChatMessage.class);
            // 存储消息
            ChatMessage chatMessage = chatService.sendMessage(message);
            Long conversationId = chatMessage.getConversationId();
            if (!Objects.equals(conversationId, message.getConversationId())){
                message.setConversationId(conversationId);
            }

            // 推送给接收方
            Session receiver = UserSessionManager.get(message.getReceiverId());

            if (receiver != null) {
                JSONObject resp = pushToReceiver(message,chatMessage);
                chatService.addAckRetryTask(chatMessage.getId(), 0);
                receiver.getBasicRemote().sendText(resp.toJSONString());
            }
        }

        else if ("ACK".equals(type)) {

            Long messageId = obj.getLong("messageId");

            chatService.ackMessage(messageId);

        }
    }

    private JSONObject pushToReceiver(ChatMessage message,ChatMessage chatMessage) {
        JSONObject resp = new JSONObject();
        resp.put("type", "CHAT");
        resp.put("messageId", chatMessage.getId().toString());
        resp.put("content", message.getContent());
        Long chatMessageConversationId = chatMessage.getConversationId();
        if (!Objects.equals(chatMessageConversationId, message.getConversationId())){
            message.setConversationId(chatMessageConversationId);
        }
        resp.put("conversationId",message.getConversationId());
        resp.put("receiverId",message.getReceiverId());

        return resp;
    }

    private void markUserOnline(Long userId, String sessionId) {
        RSet<String> userSessions = redissonClient.getSet(CHAT_ONLINE_SESSION_KEY + userId);
        userSessions.add(sessionId);
        userSessions.expire(Duration.ofHours(24));
        redissonClient.getBucket(CHAT_ONLINE_USER_KEY + userId).set(Boolean.TRUE, Duration.ofHours(24));
    }

    private void markUserOffline(Long userId, String sessionId) {
        RSet<String> userSessions = redissonClient.getSet(CHAT_ONLINE_SESSION_KEY + userId);
        userSessions.remove(sessionId);
        if (userSessions.isEmpty()) {
            userSessions.delete();
            redissonClient.getBucket(CHAT_ONLINE_USER_KEY + userId).delete();
        }
    }

}
