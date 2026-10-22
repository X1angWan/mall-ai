-- ============================================================
-- mall-ai 数据库初始化脚本（MySQL 8.x）
-- 说明：可重复执行（先 DROP 再 CREATE）；演示数据仅含分类与商品；
--       用户数据不在此插入，由应用首次启动时用代码初始化（08 章）。
-- ============================================================

CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mall;

-- ------------------------------------------------------------
-- 1. 用户表（08 章认证用，简化 RBAC：角色直接放用户表）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(20)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 哈希，绝不存明文）',
    nickname    VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role        VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：1 已删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 2. 商品分类表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_product_category;
CREATE TABLE t_product_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32) NOT NULL COMMENT '分类名称',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1 启用 0 禁用',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：1 已删除',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- ------------------------------------------------------------
-- 3. 商品表（version 乐观锁：06 章扣库存时配合使用）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT        NOT NULL COMMENT '分类 id（t_product_category.id）',
    name        VARCHAR(100)  NOT NULL COMMENT '商品名称',
    subtitle    VARCHAR(200)  DEFAULT NULL COMMENT '副标题',
    main_image  VARCHAR(500)  DEFAULT NULL COMMENT '主图 URL',
    price       DECIMAL(10,2) NOT NULL COMMENT '售价',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1 上架 0 下架',
    version     INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：1 已删除',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_name (name),
    KEY idx_category_id (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- ------------------------------------------------------------
-- 4. 订单主表（request_id 唯一索引 = 下单幂等的最后防线）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no       VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id        BIGINT        NOT NULL COMMENT '下单用户 id',
    total_amount   DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    total_quantity INT           NOT NULL COMMENT '商品总件数',
    status         INT           NOT NULL DEFAULT 10 COMMENT '状态：10待支付 20已支付 30已发货 40已完成 50已取消',
    remark         VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    request_id     VARCHAR(64)   NOT NULL COMMENT '下单幂等号（客户端生成 UUID）',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：1 已删除',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_request_id (request_id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单主表';

-- ------------------------------------------------------------
-- 5. 订单明细表（下单时商品快照：改名改价不影响历史订单）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT        NOT NULL COMMENT '订单 id（t_order.id）',
    order_no      VARCHAR(32)   NOT NULL COMMENT '订单号（冗余，便于查询）',
    product_id    BIGINT        NOT NULL COMMENT '商品 id',
    product_name  VARCHAR(100)  DEFAULT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500)  DEFAULT NULL COMMENT '商品主图快照',
    price         DECIMAL(10,2) NOT NULL COMMENT '成交单价快照',
    quantity      INT           NOT NULL COMMENT '购买数量',
    subtotal      DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：1 已删除',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';

-- ============================================================
-- 演示数据：3 个分类、6 件商品（不插入用户）
-- ============================================================
INSERT INTO t_product_category (name, sort, status) VALUES
('手机数码', 1, 1),
('服饰鞋包', 2, 1),
('家居生活', 3, 1);

INSERT INTO t_product (category_id, name, subtitle, main_image, price, stock, status) VALUES
(1, 'AI 测试机 14',      '旗舰芯片，AI 功能拉满',        'https://img.mall-ai.local/p1.jpg', 4999.00, 100, 1),
(1, 'AI 测试机 14 Pro',  '影像旗舰，AI 修图一绝',        'https://img.mall-ai.local/p2.jpg', 6999.00,  50, 1),
(1, '无线降噪耳机 Pro',  '40dB 深度降噪',                'https://img.mall-ai.local/p3.jpg',  899.00, 200, 1),
(2, '轻薄羽绒服',        '90 白鸭绒，含绒量 90%',        'https://img.mall-ai.local/p4.jpg',  399.00, 300, 1),
(2, '商务双肩包',        '防泼水面料，16 英寸仓',        'https://img.mall-ai.local/p5.jpg',  259.00, 150, 1),
(3, '智能护眼台灯',      '无频闪，色温无级调节',          'https://img.mall-ai.local/p6.jpg',  129.00, 500, 1);
