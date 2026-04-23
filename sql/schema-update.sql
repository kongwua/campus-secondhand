-- 管理员角色与商品管理功能 - 数据库迁移脚本
-- 执行时间: 2026-04-23

-- 1. User 表添加 role 字段
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员';

-- 2. 初始化管理员账号
-- 注意：请通过注册接口创建 admin 用户后，手动执行以下 SQL 将其升级为管理员
-- UPDATE user SET role = 'ADMIN' WHERE username = 'admin';