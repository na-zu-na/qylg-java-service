package com.cc.qylgjavaservice.utils;

public class RedisConstants {
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    public static final String USER_BLACK_KEY = "jwt:blacklist:";
    public static final String ADMIN_TOKEN_KEY = "jwt:admin:";
    public static final String USER_TOKEN_KEY = "jwt:user:";
    public static final String HOT_ARTICLE_KEY = "app:home:hot_articles";
    public static final String DISCOVER_LIST_KEY_PREFIX = "app:discover:list:type:";
    public static final String HOT_ARTICLE_KEY_DETAIL = "article:detail:";
    public static final String HOT_PRODUCT_KEY = "app:home:hot_products:";
    public static final String PRODUCT_KEY = "shop:all:products:";
    public static final String CS_QUEUE_KEY = "cs:queue";
    public static final String CUSTOMER_SERVICE = "cs:map";
    public static final String CHAT_ONLINE_USER_KEY = "im:online:user:";
    public static final String CHAT_ONLINE_SESSION_KEY = "im:online:session:";
    public static final String ACK_RETRY_QUEUE = "im:ack:retry_queue";
    public static final String ACK_RETRY_COUNT = "im:ack:retry_count:";
    public static final String CS_ACCEPT_STATUS_KEY = "cs:accept:status";
}
