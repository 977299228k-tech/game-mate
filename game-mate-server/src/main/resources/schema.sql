-- Game Mate 数据库初始化脚本
-- 数据库: game_mate

CREATE DATABASE IF NOT EXISTS game_mate DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE game_mate;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account` VARCHAR(50) NOT NULL COMMENT '账号(手机号)',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `balance` INT DEFAULT 0 COMMENT '余额(小时)',
    `personality` VARCHAR(50) DEFAULT 'friendly' COMMENT 'AI人格',
    `voice` VARCHAR(50) DEFAULT 'default' COMMENT '语音',
    `memory_enabled` TINYINT DEFAULT 1 COMMENT '记忆开关',
    `emotion_enabled` TINYINT DEFAULT 1 COMMENT '情感开关',
    `tactic_enabled` TINYINT DEFAULT 0 COMMENT '战术开关',
    `guide_enabled` TINYINT DEFAULT 1 COMMENT '引导开关',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`account`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 游戏表
CREATE TABLE IF NOT EXISTS `game` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '游戏名称',
    `genre` VARCHAR(50) DEFAULT NULL COMMENT '游戏类型',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    `image_url` TEXT DEFAULT NULL COMMENT '图片URL',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '主题色',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `is_custom` TINYINT DEFAULT 0 COMMENT '是否自定义',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏表';

-- 自定义游戏表
CREATE TABLE IF NOT EXISTS `custom_game` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '游戏名称',
    `genre` VARCHAR(50) DEFAULT NULL COMMENT '游戏类型',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '主题色',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义游戏表';

-- 套餐表
CREATE TABLE IF NOT EXISTS `plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `hours` INT NOT NULL COMMENT '时长(小时)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `is_popular` TINYINT DEFAULT 0 COMMENT '是否热门',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 增值服务表
CREATE TABLE IF NOT EXISTS `extra_service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '图标',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '主题色',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值服务表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_id` BIGINT NOT NULL COMMENT '套餐ID',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `hours` INT NOT NULL COMMENT '时长(小时)',
    `status` VARCHAR(20) DEFAULT 'UNPAID' COMMENT '状态',
    `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单-增值服务关联表
CREATE TABLE IF NOT EXISTS `order_extra` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `extra_id` BIGINT NOT NULL COMMENT '增值服务ID',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单-增值服务关联表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `game_id` BIGINT NOT NULL COMMENT '游戏ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色(user/assistant)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_game` (`user_id`, `game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 高光表
CREATE TABLE IF NOT EXISTS `highlight` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `game_id` BIGINT NOT NULL COMMENT '游戏ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频URL',
    `thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '缩略图',
    `duration` INT DEFAULT 0 COMMENT '时长(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高光表';

-- 用户设置表
CREATE TABLE IF NOT EXISTS `user_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `personality` VARCHAR(50) DEFAULT 'friendly' COMMENT 'AI人格',
    `voice` VARCHAR(50) DEFAULT 'default' COMMENT '语音',
    `memory_enabled` TINYINT DEFAULT 1 COMMENT '记忆开关',
    `emotion_enabled` TINYINT DEFAULT 1 COMMENT '情感开关',
    `tactic_enabled` TINYINT DEFAULT 0 COMMENT '战术开关',
    `guide_enabled` TINYINT DEFAULT 1 COMMENT '引导开关',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- 初始化预设游戏数据
INSERT INTO `game` (`name`, `genre`, `icon`, `image_url`, `color`, `description`, `tags`, `is_custom`) VALUES
('英雄联盟', 'MOBA', '/icons/lol.png', '/images/lol.jpg', '#C89B3C', '经典5v5竞技游戏', 'MOBA,竞技,团队', 0),
('王者荣耀', 'MOBA', '/icons/honor.png', '/images/honor.jpg', '#E74C3C', '国民级MOBA手游', 'MOBA,手游,竞技', 0),
('绝地求生', 'FPS', '/icons/pubg.png', '/images/pubg.jpg', '#2ECC71', '战术竞技射击游戏', 'FPS,吃鸡,战术', 0),
('CS2', 'FPS', '/icons/cs2.png', '/images/cs2.jpg', '#F39C12', '经典第一人称射击', 'FPS,射击,竞技', 0),
('原神', 'RPG', '/icons/genshin.png', '/images/genshin.jpg', '#9B59B6', '开放世界冒险游戏', 'RPG,开放世界,冒险', 0),
('DOTA2', 'MOBA', '/icons/dota2.png', '/images/dota2.jpg', '#E67E22', '经典MOBA竞技', 'MOBA,竞技,策略', 0),
('永劫无间', '动作', '/icons/naraka.png', '/images/naraka.jpg', '#1ABC9C', '武侠吃鸡', '动作,吃鸡,武侠', 0),
('Apex英雄', 'FPS', '/icons/apex.png', '/images/apex.jpg', '#E74C3C', '大逃杀射击', 'FPS,吃鸡,英雄', 0),
('星际争霸2', 'RTS', '/icons/sc2.png', '/images/sc2.jpg', '#3498DB', '经典即时战略', 'RTS,策略,竞技', 0),
('DNF', '动作', '/icons/dnf.png', '/images/dnf.jpg', '#C0392B', '横版动作网游', '动作,格斗,刷图', 0);

-- 初始化套餐数据
INSERT INTO `plan` (`name`, `hours`, `price`, `original_price`, `is_popular`) VALUES
('体验套餐', 2, 9.90, 19.90, 0),
('基础套餐', 10, 39.90, 59.90, 0),
('标准套餐', 30, 99.90, 149.90, 1),
('高级套餐', 60, 179.90, 239.90, 0),
('至尊套餐', 120, 299.90, 399.90, 0);

-- 初始化增值服务数据
INSERT INTO `extra_service` (`name`, `description`, `icon`, `color`, `price`) VALUES
('战术大师', '解锁AI战术分析功能', '/icons/tactic.png', '#E74C3C', 19.90),
('情感陪伴', '增强AI情感交互', '/icons/emotion.png', '#9B59B6', 9.90),
('记忆增强', 'AI长期记忆对话内容', '/icons/memory.png', '#3498DB', 14.90),
('专属语音', '解锁专属AI语音包', '/icons/voice.png', '#F39C12', 6.90);


-- 用户增值服务关联表迁移脚本
-- 用于支持不同账号独立的增值服务数据

CREATE TABLE IF NOT EXISTS `user_extra_service` (
                                                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                    `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                                    `extra_id` BIGINT NOT NULL COMMENT '增值服务ID',
                                                    `total_hours` INT DEFAULT 0 COMMENT '总时长(小时)',
                                                    `used_hours` INT DEFAULT 0 COMMENT '已使用时长(小时)',
                                                    `paid_price` DECIMAL(10,2) DEFAULT 0 COMMENT '已支付金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_extra` (`user_id`, `extra_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户增值服务关联表';
