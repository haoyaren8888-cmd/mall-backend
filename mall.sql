DROP DATABASE IF EXISTS mall;
CREATE DATABASE mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mall;

CREATE TABLE `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(50) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  cover_image VARCHAR(500),
  sales INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_product_category (category_id),
  INDEX idx_product_status (status)
);

CREATE TABLE cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  checked TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cart_user_product (user_id, product_id)
);

CREATE TABLE address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  detail VARCHAR(200) NOT NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_address_user (user_id)
);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  address_id BIGINT NOT NULL,
  receiver_snapshot VARCHAR(500) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAY',
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_orders_user (user_id),
  INDEX idx_orders_status (status)
);

CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(120) NOT NULL,
  product_image VARCHAR(500),
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_order_item_order (order_id)
);

CREATE TABLE payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(40) NOT NULL,
  pay_no VARCHAR(40) NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  pay_type VARCHAR(20) NOT NULL DEFAULT 'MOCK',
  paid_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO `user` (username, password, nickname, phone, role) VALUES
('admin', '123456', '商城管理员', '13800000000', 'ADMIN'),
('user', '123456', '普通用户', '13900000000', 'USER');

INSERT INTO category (id, parent_id, name, sort) VALUES
(1, 0, '数码家电', 1),
(2, 0, '生活百货', 2),
(3, 0, '食品生鲜', 3),
(4, 0, '服饰鞋包', 4),
(11, 1, '手机电脑', 1),
(12, 1, '厨房电器', 2),
(21, 2, '清洁日用', 1),
(22, 2, '家居收纳', 2),
(31, 3, '休闲零食', 1),
(32, 3, '水果饮品', 2),
(41, 4, '男装女装', 1),
(42, 4, '运动鞋包', 2);

INSERT INTO product (category_id, name, description, price, stock, cover_image, sales) VALUES
(11, '轻薄办公笔记本', '16G 内存，512G 固态硬盘，适合学习和办公。', 4599.00, 35, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80', 18),
(11, '高清全面屏手机', '高刷屏幕，长续航，适合日常使用。', 2399.00, 80, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=80', 52),
(12, '多功能空气炸锅', '少油烹饪，一锅多用。', 399.00, 60, 'https://images.unsplash.com/photo-1585515320310-259814833e62?auto=format&fit=crop&w=900&q=80', 31),
(21, '家庭清洁套装', '厨房、浴室、地面清洁组合。', 59.90, 200, 'https://images.unsplash.com/photo-1583947581924-860bda6a26df?auto=format&fit=crop&w=900&q=80', 77),
(22, '桌面收纳盒', '分格设计，书桌更整洁。', 29.90, 150, 'https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?auto=format&fit=crop&w=900&q=80', 44),
(31, '每日坚果礼盒', '混合坚果，独立包装。', 89.00, 120, 'https://images.unsplash.com/photo-1508747703725-719777637510?auto=format&fit=crop&w=900&q=80', 65),
(32, '鲜榨果汁组合', '多口味果汁，冷藏配送。', 49.90, 90, 'https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=900&q=80', 27),
(41, '休闲连帽卫衣', '柔软面料，春秋百搭。', 159.00, 70, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=900&q=80', 38),
(42, '轻便运动背包', '大容量，多隔层，适合通勤和运动。', 129.00, 85, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80', 21);

INSERT INTO address (user_id, receiver_name, receiver_phone, province, city, district, detail, is_default) VALUES
(2, '张同学', '13900000000', '四川省', '成都市', '武侯区', '软件园 A 区 1 栋 101', 1);
