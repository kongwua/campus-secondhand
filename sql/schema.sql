-- 校园二手物品交易系统 - 数据库表结构
-- 字符集: utf8mb4
-- 存储引擎: InnoDB

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `avatar_url` VARCHAR(500) COMMENT '头像URL',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `credit_score` INT DEFAULT 100 COMMENT '信用分',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-正常, 1-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` INT DEFAULT 0 COMMENT '父分类ID, 0表示顶级分类',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 3. 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '卖家ID',
    `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
    `description` TEXT COMMENT '商品描述',
    `category_id` INT COMMENT '分类ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10, 2) COMMENT '原价',
    `images` JSON COMMENT '图片URL数组',
    `location` VARCHAR(100) COMMENT '交易地点',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-在售, 1-已售, 2-下架',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 4. 交易表
CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '交易价格',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待确认, 1-进行中, 2-已完成, 3-已取消',
    `meeting_time` DATETIME COMMENT '约定交易时间',
    `meeting_location` VARCHAR(200) COMMENT '交易地点',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time` DATETIME COMMENT '完成时间',
    PRIMARY KEY (`id`),
    INDEX `idx_buyer_id` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易表';

-- 5. 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `product_id` BIGINT COMMENT '关联商品ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 6. 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `transaction_id` BIGINT NOT NULL COMMENT '交易ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评价者ID',
    `reviewed_id` BIGINT NOT NULL COMMENT '被评价者ID',
    `rating` INT NOT NULL COMMENT '评分: 1-5',
    `content` TEXT COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_reviewed_id` (`reviewed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
