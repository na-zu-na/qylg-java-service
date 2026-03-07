-- 1. 主页轮播图 (home_banners)
CREATE TABLE home_banners (
                              id SERIAL PRIMARY KEY,
                              image VARCHAR NOT NULL
);


-- 2. 文章数据表 (articles)
CREATE TABLE articles (
                          id BIGSERIAL PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,
                          author_id BIGINT NOT NULL,
                          category_id INTEGER NOT NULL,
                          content TEXT NOT NULL,
                          images JSONB,
                          status SMALLINT NOT NULL DEFAULT 1, -- 1-发布，2-下架
                          published_at TIMESTAMP NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_articles_category FOREIGN KEY (category_id) REFERENCES article_categories(id) ON DELETE RESTRICT

);
COMMENT ON COLUMN articles.status IS '状态：1-发布，2-下架';

-- 3. 文章种类表 (article_categories)
CREATE TABLE article_categories (
                                    id BIGSERIAL PRIMARY KEY,
                                    type VARCHAR(50) NOT NULL,
                                    name VARCHAR(50) NOT NULL
);


-- 4. 文章点赞表 (article_likes)
CREATE TABLE article_likes (
                               id BIGSERIAL PRIMARY KEY,
                               article_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT uk_article_user UNIQUE (article_id, user_id),
                               CONSTRAINT fk_likes_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
                               CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
COMMENT ON CONSTRAINT uk_article_user ON article_likes IS '防止重复点赞';

-- 5. 文章评论数据表 (article_comments)
CREATE TABLE article_comments (
                                  id BIGSERIAL PRIMARY KEY,
                                  article_id BIGINT NOT NULL,
                                  user_id BIGINT NOT NULL,
                                  parent_id BIGINT,
                                  root_id BIGINT NOT NULL DEFAULT 0,
                                  reply_to_user_id BIGINT,
                                  content TEXT NOT NULL,
                                  status SMALLINT NOT NULL DEFAULT 1, -- 0-待审核/隐藏，1-正常显示
                                  created_at TIMESTAMP NOT NULL,

                                  CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES article_comments(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_comments_root FOREIGN KEY (root_id) REFERENCES article_comments(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_comments_reply_user FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_article_comments_query ON article_comments (article_id, root_id, created_at);

-- 6. 用户数据表 (users)
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       openid VARCHAR(64),
                       phone VARCHAR(20),
                       password VARCHAR(255),
                       role_code VARCHAR(20) NOT NULL DEFAULT 'user',
                       status SMALLINT NOT NULL DEFAULT 1, -- 0-封禁，1-正常，2-待审核
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT uk_users_openid UNIQUE (openid),
                       CONSTRAINT uk_users_phone UNIQUE (phone)
);


-- 7. 商品表 (products)
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          type SMALLINT NOT NULL DEFAULT 1, -- 1-普通商品，2-定制商品
                          title VARCHAR(255) NOT NULL,
                          cover VARCHAR(255) NOT NULL,
                          price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                          min_price NUMERIC(10, 2),
                          max_price NUMERIC(10, 2),
                          anchor VARCHAR(255),
                          "desc" TEXT NOT NULL,
                          images JSONB,
                          purpose VARCHAR(100),
                          style VARCHAR(100),
                          material VARCHAR(100),
                          total_sales INTEGER NOT NULL DEFAULT 0,
                          status SMALLINT NOT NULL DEFAULT 1, -- 0-下架，1-上架
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 8. 商品评论表 (product_reviews)
CREATE TABLE product_reviews (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 goods_id BIGINT NOT NULL,
                                 order_id BIGINT,
                                 content TEXT NOT NULL,
                                 rate SMALLINT NOT NULL, -- 评分 1-5
                                 images JSONB,
                                 created_at TIMESTAMP NOT NULL,

                                 CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_reviews_product FOREIGN KEY (goods_id) REFERENCES products(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);
CREATE INDEX idx_product_reviews_query ON product_reviews (goods_id, created_at);

-- 9. 购物车表 (shopping_cart)
CREATE TABLE shopping_cart (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               goods_id BIGINT NOT NULL,
                               count INTEGER NOT NULL,
                               created_at TIMESTAMP NOT NULL,

                               CONSTRAINT uk_cart_user_goods UNIQUE (user_id, goods_id),
                               CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_cart_product FOREIGN KEY (goods_id) REFERENCES products(id) ON DELETE CASCADE
);


-- 10. 地址表 (addresses)
CREATE TABLE addresses (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           name VARCHAR(50) NOT NULL,
                           phone VARCHAR(20) NOT NULL,
                           location VARCHAR(255) NOT NULL,

                           CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_addresses_user ON addresses (user_id);


-- 11. 定制订单表 (custom_orders)
CREATE TABLE custom_orders (
                               id BIGSERIAL PRIMARY KEY,
                               order_no VARCHAR(64) NOT NULL,
                               user_id BIGINT NOT NULL,
                               goods_name VARCHAR(255) NOT NULL,
                               purpose VARCHAR(100) NOT NULL,
                               style VARCHAR(100) NOT NULL,
                               material VARCHAR(100) NOT NULL,
                               min_budget NUMERIC(10, 2),
                               max_budget NUMERIC(10, 2),
                               colors JSONB,
                               patterns JSONB,
                               size VARCHAR(50) NOT NULL,
                               remark TEXT,
                               images JSONB,
                               status VARCHAR NOT NULL, -- 'making', 'shipping', 'finished'
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uk_custom_order_no UNIQUE (order_no),
                               CONSTRAINT fk_custom_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);


-- 12. 颜色纹路标签表 (tags)
CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      type VARCHAR(20) NOT NULL, -- 'color' or 'pattern'
                      key VARCHAR(50) NOT NULL,
                      text VARCHAR(50) NOT NULL
);
CREATE INDEX idx_tags_type_key ON tags (type, key);

-- 13. 普通商品订单表 (orders)
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_no VARCHAR(64) NOT NULL,
                        user_id BIGINT NOT NULL,
                        status SMALLINT NOT NULL, -- 1-待收货，2-已完成
                        total_price NUMERIC(10, 2) NOT NULL,
                        total_count INTEGER NOT NULL,
                        receiver_name VARCHAR(50) NOT NULL,
                        receiver_phone VARCHAR(20) NOT NULL,
                        receiver_address VARCHAR(255) NOT NULL,
                        pay_time TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT uk_order_no UNIQUE (order_no)
);
COMMENT ON TABLE orders IS '普通商品订单表';

-- 14. 订单商品关联表 (order_items)
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             goods_id BIGINT NOT NULL,
                             count INTEGER NOT NULL,
                             price NUMERIC(10, 2) NOT NULL,

                             CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_items_product FOREIGN KEY (goods_id) REFERENCES products(id) ON DELETE RESTRICT
);
CREATE INDEX idx_order_items_order ON order_items (order_id);

-- 15. 聊天记录表 (chat_messages)
CREATE TABLE chat_messages (
                               id BIGSERIAL PRIMARY KEY,
                               order_no VARCHAR(64),
                               user_id BIGINT NOT NULL,
                               role VARCHAR(10) NOT NULL,
                               content TEXT NOT NULL,
                               msg_type VARCHAR(10) NOT NULL DEFAULT 'text',
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_chat_role CHECK (role IN ('user', 'ai')),
                               CONSTRAINT chk_chat_msg_type CHECK (msg_type IN ('text', 'image')),
                               CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES  users(id) ON DELETE CASCADE

);
CREATE INDEX idx_chat_messages_session ON chat_messages (order_no, created_at);

-- 16. AI配置表 (ai_configs)
CREATE TABLE ai_configs (
                            id BIGSERIAL PRIMARY KEY,
                            apikey VARCHAR(255) NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT uk_ai_config_name UNIQUE (name)
);


ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

alter TABLE articles ADD
    CONSTRAINT fk_articles_category FOREIGN KEY (category_id) REFERENCES article_categories(id) ON DELETE RESTRICT