CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mall;

CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  student_no VARCHAR(30),
  campus VARCHAR(50) DEFAULT '明向校区',
  college VARCHAR(80),
  dormitory VARCHAR(80),
  credit_score INT NOT NULL DEFAULT 100,
  deal_count INT NOT NULL DEFAULT 0,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_student_no (student_no)
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
  seller_id BIGINT,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  original_price DECIMAL(10,2),
  stock INT NOT NULL DEFAULT 1,
  cover_image VARCHAR(500),
  sales INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  condition_level VARCHAR(20) NOT NULL DEFAULT '九成新',
  campus VARCHAR(50) NOT NULL DEFAULT '明向校区',
  trade_place VARCHAR(120),
  trade_type VARCHAR(30) NOT NULL DEFAULT '线下面交',
  audit_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
  item_status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
  view_count INT NOT NULL DEFAULT 0,
  favorite_count INT NOT NULL DEFAULT 0,
  reject_reason VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_product_category (category_id),
  KEY idx_product_seller (seller_id),
  KEY idx_product_status (status),
  KEY idx_product_audit (audit_status),
  KEY idx_product_item_status (item_status),
  KEY idx_product_campus (campus),
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

INSERT INTO `user`
  (id, username, password, nickname, phone, student_no, campus, college, dormitory, credit_score, deal_count, role, status)
VALUES
  (1, 'admin', '123456', '任亚浩', '13800000000', '20260001', '明向校区', '信息与计算机学院', '后台管理', 100, 0, 'ADMIN', 'ENABLED'),
  (2, 'user', '123456', '王嘉毅', '13900000000', '20260002', '明向校区', '软件学院', '清泽苑3号楼', 96, 3, 'USER', 'ENABLED'),
  (3, 'seller', '123456', '赵奎', '13700000000', '20260003', '迎西校区', '机械与运载工程学院', '智林公寓2号楼', 98, 5, 'USER', 'ENABLED'),
  (4, 'buyer', '123456', '李炯标', '13600000000', '20260004', '虎峪校区', '经济管理学院', '学生公寓6号楼', 94, 2, 'USER', 'ENABLED')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  phone = VALUES(phone),
  student_no = VALUES(student_no),
  campus = VALUES(campus),
  college = VALUES(college),
  dormitory = VALUES(dormitory),
  credit_score = VALUES(credit_score),
  deal_count = VALUES(deal_count),
  role = VALUES(role),
  status = VALUES(status);

INSERT INTO category (id, parent_id, name, sort, status)
VALUES
  (1, 0, '数码电子', 10, 'ON'),
  (2, 0, '教材资料', 20, 'ON'),
  (3, 0, '宿舍生活', 30, 'ON'),
  (4, 0, '运动出行', 40, 'ON'),
  (5, 0, '校园服务', 50, 'ON'),
  (11, 1, '手机电脑', 11, 'ON'),
  (12, 1, '耳机相机', 12, 'ON'),
  (21, 2, '考研教材', 21, 'ON'),
  (22, 2, '专业课资料', 22, 'ON'),
  (31, 3, '小家电', 31, 'ON'),
  (32, 3, '收纳日用', 32, 'ON'),
  (41, 4, '球拍护具', 41, 'ON'),
  (42, 4, '自行车滑板', 42, 'ON'),
  (51, 5, '打印代取', 51, 'ON'),
  (52, 5, '技能互助', 52, 'ON')
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT INTO product
  (id, category_id, seller_id, name, description, price, original_price, stock, cover_image, sales, status,
   condition_level, campus, trade_place, trade_type, audit_status, item_status, view_count, favorite_count, reject_reason)
VALUES
  (1, 11, 2, '九成新 ThinkPad X1 Carbon', '大三课程设计期间主要用来写代码，键盘手感好，电池正常，附原装充电器。', 2680.00, 8999.00, 1, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853', 0, 'ON', '九成新', '明向校区', '行知楼B座门口', '线下面交', 'APPROVED', 'ON_SALE', 138, 12, NULL),
  (2, 11, 3, 'iPad Air 5 64G 深空灰', '屏幕无划痕，平时只看网课和做笔记，可附送保护壳。', 2799.00, 4399.00, 1, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0', 0, 'ON', '九成新', '迎西校区', '图书馆一层大厅', '线下面交', 'APPROVED', 'ON_SALE', 91, 9, NULL),
  (3, 21, 4, '高等数学同济第七版上下册', '书角有轻微折痕，内页笔记不多，适合补课和期末复习。', 28.00, 86.00, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794', 0, 'ON', '八成新', '明向校区', '清泽餐厅门口', '线下面交', 'APPROVED', 'ON_SALE', 64, 7, NULL),
  (4, 22, 2, '数据结构课程设计参考资料', '包含往年实验整理和自己整理的思维导图，纸质打印版。', 18.00, 45.00, 1, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3', 0, 'ON', '七成新', '明向校区', '计算机学院楼下', '线下面交', 'APPROVED', 'ON_SALE', 56, 4, NULL),
  (5, 31, 3, '宿舍小冰箱 46L', '毕业搬宿舍出，制冷正常，适合放饮料和水果，需要自提。', 220.00, 599.00, 1, 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5', 0, 'ON', '八成新', '迎西校区', '智林公寓2号楼下', '线下面交', 'APPROVED', 'ON_SALE', 112, 15, NULL),
  (6, 32, 4, '桌面收纳架三层', '铁艺三层架，放教材和键盘都可以，宿舍桌面更整齐。', 25.00, 79.00, 1, 'https://images.unsplash.com/photo-1497366754035-f200968a6e72', 0, 'ON', '九成新', '虎峪校区', '致明楼门口', '线下面交', 'APPROVED', 'ON_SALE', 43, 3, NULL),
  (7, 41, 2, 'Yonex 羽毛球拍', '轻量拍，拍线刚换不久，适合日常打球。', 120.00, 399.00, 1, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea', 0, 'ON', '八成新', '明向校区', '体育馆北门', '线下面交', 'APPROVED', 'ON_SALE', 76, 8, NULL),
  (8, 42, 3, '捷安特山地车', '通勤骑行正常，刹车已调，适合明向校区内代步。', 480.00, 1599.00, 1, 'https://images.unsplash.com/photo-1485965120184-e220f721d03e', 0, 'ON', '七成新', '明向校区', '西门停车区', '线下面交', 'APPROVED', 'ON_SALE', 183, 21, NULL),
  (9, 12, 4, '索尼降噪耳机 WH-1000XM4', '耳罩轻微使用痕迹，降噪正常，支持现场试机。', 780.00, 2299.00, 1, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', 0, 'ON', '九成新', '迎西校区', '主楼前广场', '线下面交', 'APPROVED', 'ON_SALE', 127, 16, NULL),
  (10, 51, 2, '明向校区资料打印代取', '可帮忙取打印店资料，晚上统一送到清泽苑门口。', 3.00, NULL, 20, 'https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6', 0, 'ON', '全新', '明向校区', '清泽苑门口', '校内自取', 'APPROVED', 'ON_SALE', 49, 5, NULL),
  (11, 52, 3, 'Java Web 课程设计答疑', '提供 Spring Boot、Vue 联调问题答疑，只做思路讲解和问题定位。', 20.00, NULL, 8, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3', 0, 'ON', '全新', '迎西校区', '图书馆讨论室', '预约面交', 'APPROVED', 'ON_SALE', 72, 11, NULL),
  (12, 22, 4, '待审核的数据库笔记整理', '刚发布的专业课资料，等待管理员审核通过后展示。', 15.00, 35.00, 1, 'https://images.unsplash.com/photo-1517842645767-c639042777db', 0, 'ON', '八成新', '虎峪校区', '经管楼门口', '线下面交', 'PENDING', 'ON_SALE', 0, 0, NULL)
ON DUPLICATE KEY UPDATE
  category_id = VALUES(category_id),
  seller_id = VALUES(seller_id),
  name = VALUES(name),
  description = VALUES(description),
  price = VALUES(price),
  original_price = VALUES(original_price),
  stock = VALUES(stock),
  cover_image = VALUES(cover_image),
  sales = VALUES(sales),
  status = VALUES(status),
  condition_level = VALUES(condition_level),
  campus = VALUES(campus),
  trade_place = VALUES(trade_place),
  trade_type = VALUES(trade_type),
  audit_status = VALUES(audit_status),
  item_status = VALUES(item_status),
  view_count = VALUES(view_count),
  favorite_count = VALUES(favorite_count),
  reject_reason = VALUES(reject_reason);

INSERT INTO address (id, user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
VALUES
  (1, 2, '王嘉毅', '13900000000', '山西省', '太原市', '小店区', '太原理工大学明向校区清泽苑3号楼', 1),
  (2, 3, '赵奎', '13700000000', '山西省', '太原市', '迎泽区', '太原理工大学迎西校区智林公寓2号楼', 1),
  (3, 4, '李炯标', '13600000000', '山西省', '太原市', '万柏林区', '太原理工大学虎峪校区学生公寓6号楼', 1)
ON DUPLICATE KEY UPDATE
  receiver_name = VALUES(receiver_name),
  receiver_phone = VALUES(receiver_phone),
  province = VALUES(province),
  city = VALUES(city),
  district = VALUES(district),
  detail = VALUES(detail),
  is_default = VALUES(is_default);
