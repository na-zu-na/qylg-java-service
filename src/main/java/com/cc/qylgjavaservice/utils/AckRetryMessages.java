package com.cc.qylgjavaservice.utils;

import com.alibaba.fastjson2.JSONObject;
import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.mapper.ChatMessageMapper;
import com.cc.qylgjavaservice.service.impl.ChatServiceImpl;
import com.cc.qylgjavaservice.websocket.UserSessionManager;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.cc.qylgjavaservice.utils.RedisConstants.ACK_RETRY_COUNT;
import static com.cc.qylgjavaservice.utils.RedisConstants.ACK_RETRY_QUEUE;

@Component
@Slf4j
public class AckRetryMessages {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatServiceImpl chatService;

    private static final String LUA_SCRIPT="local zsetKey = KEYS[1]" +
            "local countPrefix = KEYS[2] " +
            "local now = tonumber(ARGV[1]) " +
            "local limit = tonumber(ARGV[2]) " +
            "local entries = redis.call('ZRANGEBYSCORE', zsetKey, 0, now, 'LIMIT', 0, limit) " +
            "if (#entries == 0) then " +
            "return {}" +
            "end " +
            "local result = {}" +
            "for i, messageId in ipairs(entries) do " +
            "redis.call('ZREM', zsetKey, messageId) " +
            "local retryKey = countPrefix .. messageId " +
            "local retryCount = redis.call('GET', retryKey) " +
            "if (not retryCount) then " +
            "retryCount = '0' " +
            "end " +
            "table.insert(result, messageId) " +
            "table.insert(result, retryCount) " +
            "end " +
            "return result";

    public List<Object> fetchRetryTasks(){
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);

        return script.eval(
                RScript.Mode.READ_WRITE,
                LUA_SCRIPT,
                RScript.ReturnType.LIST,
                Arrays.asList(ACK_RETRY_QUEUE, ACK_RETRY_COUNT),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(50)
        );
    }

    @Scheduled(fixedDelay = 2000)
    public void retryJob() {

        List<Object> result = fetchRetryTasks();

        if (result == null || result.isEmpty()) {
            return;
        }

        for (int i = 0; i < result.size(); i += 2) {
            Long messageId = Long.valueOf((String) result.get(i));
            Integer retryCount = Integer.valueOf((String) result.get(i + 1));

            handleRetry(messageId, retryCount);
        }
    }

    private void handleRetry(Long messageId, Integer retryCount) {

        // 最大重试次数
        if (retryCount >= 5) {
            log.warn("消息重试失败 messageId={}", messageId);
            handleDeadMessage(messageId);
            return;
        }

        ChatMessage msg = chatMessageMapper.selectById(messageId);

        if (msg == null) {
            return;
        }

        // 已经ACK了
        if (msg.getStatus() == 2) {
            return;
        }

        Session receiver = UserSessionManager.get(msg.getReceiverId());

        try {
            if (receiver != null) {
                receiver.getBasicRemote().sendText(buildRetryMsg(msg).toJSONString());
            }
        } catch (Exception e) {
            log.error("重试发送失败 messageId={}", messageId, e);
        }

        // 重新入队（指数退避）
        chatService.addAckRetryTask(messageId, retryCount + 1);
    }

    private JSONObject buildRetryMsg(ChatMessage message) {
        JSONObject resp = new JSONObject();
        resp.put("type", "CHAT");
        resp.put("messageId", message.getId().toString());
        resp.put("content", message.getContent());
        resp.put("conversationId",message.getConversationId());
        resp.put("receiverId",message.getReceiverId());

        return resp;
    }

    private void handleDeadMessage(Long messageId) {

        ChatMessage msg = new ChatMessage();
        msg.setId(messageId);
        msg.setStatus(3); // 失败

        chatMessageMapper.updateById(msg);

        log.error("消息进入死信队列 messageId={}", messageId);
    }
}
