package com.cc.qylgjavaservice.websocket;


import jakarta.websocket.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {

    private static final Map<Long, Session> USER_SESSION=new ConcurrentHashMap<>();

    public static void add(Long userId, Session session){
        USER_SESSION.put(userId,session);
    }

    public static void remove(Long userId){
        USER_SESSION.remove(userId);
    }

    public static Session get(Long userId){
        return USER_SESSION.get(userId);
    }
}
