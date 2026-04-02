package com.cc.qylgjavaservice.websocket;


import jakarta.websocket.Session;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {

    private static final Map<Long, Map<String, Session>> USER_SESSION = new ConcurrentHashMap<>();

    public static void add(Long userId, Session session){
        USER_SESSION.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
    }

    public static void remove(Long userId, Session session){
        Map<String, Session> sessions = USER_SESSION.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session.getId());
        if (sessions.isEmpty()) {
            USER_SESSION.remove(userId);
        }
    }

    public static Session get(Long userId){
        Map<String, Session> sessions = USER_SESSION.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return null;
        }

        Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Session session = iterator.next().getValue();
            if (session != null && session.isOpen()) {
                return session;
            }
            iterator.remove();
        }

        if (sessions.isEmpty()) {
            USER_SESSION.remove(userId);
        }
        return null;
    }
}
