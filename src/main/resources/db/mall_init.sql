CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(80) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_category_parent (parent_id),
  KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
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
  KEY idx_product_category (category_id),
  KEY idx_product_status (status),
  KEY idx_product_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  checked TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cart_user_product (user_id, product_id),
  KEY idx_cart_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_address_user (user_id),
  KEY idx_address_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL,
  user_id BIGINT NOT NULL,
  address_id BIGINT NOT NULL,
  receiver_snapshot VARCHAR(500) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAY',
  paid_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user (user_id),
  KEY idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(120) NOT NULL,
  product_image VARCHAR(500),
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order_item_order (order_id),
  KEY idx_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(40) NOT NULL,
  pay_no VARCHAR(40) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  pay_type VARCHAR(20) NOT NULL,
  paid_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payment_pay_no (pay_no),
  KEY idx_payment_order (order_id),
  KEY idx_payment_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (id, username, password, nickname, phone, role, status)
VALUES
  (1, 'admin', '123456', '商城管理员', '13800000000', 'ADMIN', 'ENABLED'),
  (2, 'user', '123456', '普通用户', '13900000000', 'USER', 'ENABLED')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  phone = VALUES(phone),
  role = VALUES(role),
  status = VALUES(status);

INSERT INTO category (id, parent_id, name, sort, status)
VALUES
  (1, 0, '数码家电', 10, 'ON'),
  (2, 0, '居家生活', 20, 'ON'),
  (3, 0, '图书文具', 30, 'ON'),
  (4, 1, '手机通讯', 11, 'ON'),
  (5, 1, '电脑办公', 12, 'ON'),
  (6, 2, '厨房用品', 21, 'ON'),
  (7, 2, '清洁收纳', 22, 'ON'),
  (8, 3, '课程资料', 31, 'ON')
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT INTO product (id, category_id, name, description, price, stock, cover_image, sales, status)
VALUES
  (1, 4, '星河 X1 智能手机', '6.7 英寸高清屏，适合日常学习、拍照和移动办公。', 2999.00, 60, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9', 18, 'ON'),
  (2, 5, '轻薄办公笔记本', '16G 内存和 512G 固态硬盘，适合课程设计和日常开发。', 4599.00, 35, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853', 12, 'ON'),
  (3, 4, '蓝牙降噪耳机', '通勤、网课和运动都能使用的入耳式耳机。', 299.00, 120, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', 46, 'ON'),
  (4, 6, '多功能电饭煲', '预约煮饭、保温和煲汤一体，适合宿舍和家庭。', 399.00, 80, 'https://images.unsplash.com/photo-1589881133825-bbb3b9471b1b', 21, 'ON'),
  (5, 7, '桌面收纳套装', '整理键盘、文具和数据线，保持学习桌整洁。', 69.90, 200, 'https://images.unsplash.com/photo-1497366754035-f200968a6e72', 73, 'ON'),
  (6, 8, 'Java Web 课程设计指南', '覆盖 Spring Boot、MyBatis 和前后端联调的入门资料。', 58.00, 90, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3', 34, 'ON'),
  (7, 5, '机械键盘', '青轴手感，适合编程和文字输入。', 189.00, 100, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3', 27, 'ON'),
  (8, 6, '不锈钢保温杯', '500ml 容量，适合课堂、办公室和出行携带。', 49.90, 150, 'https://images.unsplash.com/photo-1523362628745-0c100150b504', 55, 'ON')
ON DUPLICATE KEY UPDATE
  category_id = VALUES(category_id),
  name = VALUES(name),
  description = VALUES(description),
  price = VALUES(price),
  stock = VALUES(stock),
  cover_image = VALUES(cover_image),
  sales = VALUES(sales),
  status = VALUES(status);

INSERT INTO address (id, user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
VALUES
  (1, 2, '普通用户', '13900000000', '河南省', '郑州市', '金水区', '文化路 100 号 1 栋 101', 1)
ON DUPLICATE KEY UPDATE
  receiver_name = VALUES(receiver_name),
  receiver_phone = VALUES(receiver_phone),
  province = VALUES(province),
  city = VALUES(city),
  district = VALUES(district),
  detail = VALUES(detail),
  is_default = VALUES(is_default);
