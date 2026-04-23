-- 校园二手交易系统 - 初始化数据

-- 示例用户 (密码: test123)
INSERT INTO user (id, username, password, nickname, phone, email, credit_score, status) VALUES
(1, 'testuser', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaW8T8lQ8gXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9', '测试用户', '13800138001', 'test@example.com', 100, 0),
(2, 'seller1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaW8T8lQ8gXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9', '卖家小明', '13800138002', 'seller1@example.com', 95, 0),
(3, 'buyer1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaW8T8lQ8gXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9kXZ9', '买家小红', '13800138003', 'buyer1@example.com', 100, 0);

-- 示例商品 (status=1表示在售，对应后端ProductService的STATUS_ACTIVE)
INSERT INTO product (id, user_id, title, description, category_id, price, original_price, images, location, status, view_count) VALUES
(1, 1, '二手MacBook Pro 2020', '15寸 MacBook Pro，8GB内存，256GB SSD，成色良好', 1, 4500.00, 12000.00, '["products/macbook.jpg"]', '图书馆一楼', 1, 56),
(2, 1, '高数教材全套', '高等数学第七版上下册，有少量笔记', 2, 35.00, 80.00, '["products/math.jpg"]', '教学楼A区', 1, 23),
(3, 2, '小米蓝牙耳机', '小米Air 2 SE，使用半年，音质不错', 1, 89.00, 199.00, '["products/earphone.jpg"]', '宿舍楼下', 1, 18),
(4, 2, '台灯护眼灯', '飞利浦护眼台灯，三档调节，无损坏', 3, 45.00, 120.00, '["products/lamp.jpg"]', '食堂门口', 1, 12),
(5, 3, '瑜伽垫加厚', '6mm加厚瑜伽垫，紫色，已清洗', 4, 25.00, 68.00, '["products/yoga.jpg"]', '操场旁', 1, 8),
(6, 3, '四六级词汇书', '新东方四级词汇+六级词汇，正版', 2, 15.00, 50.00, '["products/vocab.jpg"]', '自习室', 1, 31);