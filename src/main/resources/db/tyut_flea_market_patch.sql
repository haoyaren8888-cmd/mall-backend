USE mall;

DELIMITER //
CREATE PROCEDURE add_column_if_missing(IN table_name_value VARCHAR(64), IN column_name_value VARCHAR(64), IN column_sql TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = table_name_value
      AND column_name = column_name_value
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD COLUMN ', column_sql);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_index_if_missing(IN table_name_value VARCHAR(64), IN index_name_value VARCHAR(64), IN index_sql TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = table_name_value
      AND index_name = index_name_value
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD ', index_sql);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('user', 'student_no', 'student_no VARCHAR(30) NULL AFTER phone');
CALL add_column_if_missing('user', 'campus', 'campus VARCHAR(50) NULL DEFAULT ''明向校区'' AFTER student_no');
CALL add_column_if_missing('user', 'college', 'college VARCHAR(80) NULL AFTER campus');
CALL add_column_if_missing('user', 'dormitory', 'dormitory VARCHAR(80) NULL AFTER college');
CALL add_column_if_missing('user', 'credit_score', 'credit_score INT NOT NULL DEFAULT 100 AFTER dormitory');
CALL add_column_if_missing('user', 'deal_count', 'deal_count INT NOT NULL DEFAULT 0 AFTER credit_score');
CALL add_index_if_missing('user', 'uk_user_student_no', 'UNIQUE KEY uk_user_student_no (student_no)');

CALL add_column_if_missing('product', 'seller_id', 'seller_id BIGINT NULL AFTER category_id');
CALL add_column_if_missing('product', 'original_price', 'original_price DECIMAL(10,2) NULL AFTER price');
CALL add_column_if_missing('product', 'condition_level', 'condition_level VARCHAR(20) NOT NULL DEFAULT ''九成新'' AFTER status');
CALL add_column_if_missing('product', 'campus', 'campus VARCHAR(50) NOT NULL DEFAULT ''明向校区'' AFTER condition_level');
CALL add_column_if_missing('product', 'trade_place', 'trade_place VARCHAR(120) NULL AFTER campus');
CALL add_column_if_missing('product', 'trade_type', 'trade_type VARCHAR(30) NOT NULL DEFAULT ''线下面交'' AFTER trade_place');
CALL add_column_if_missing('product', 'audit_status', 'audit_status VARCHAR(20) NOT NULL DEFAULT ''APPROVED'' AFTER trade_type');
CALL add_column_if_missing('product', 'item_status', 'item_status VARCHAR(20) NOT NULL DEFAULT ''ON_SALE'' AFTER audit_status');
CALL add_column_if_missing('product', 'view_count', 'view_count INT NOT NULL DEFAULT 0 AFTER item_status');
CALL add_column_if_missing('product', 'favorite_count', 'favorite_count INT NOT NULL DEFAULT 0 AFTER view_count');
CALL add_column_if_missing('product', 'reject_reason', 'reject_reason VARCHAR(255) NULL AFTER favorite_count');
CALL add_index_if_missing('product', 'idx_product_seller', 'KEY idx_product_seller (seller_id)');
CALL add_index_if_missing('product', 'idx_product_audit', 'KEY idx_product_audit (audit_status)');
CALL add_index_if_missing('product', 'idx_product_campus', 'KEY idx_product_campus (campus)');

CREATE TABLE IF NOT EXISTS product_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite_user_product (user_id, product_id),
  KEY idx_favorite_user (user_id),
  KEY idx_favorite_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(300) NOT NULL,
  reply_content VARCHAR(300) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reply_at DATETIME NULL,
  KEY idx_message_product (product_id),
  KEY idx_message_user (user_id),
  KEY idx_message_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CALL add_column_if_missing('product_message', 'reply_content', 'reply_content VARCHAR(300) NULL AFTER content');
CALL add_column_if_missing('product_message', 'reply_at', 'reply_at DATETIME NULL AFTER created_at');

CREATE TABLE IF NOT EXISTS product_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_review_order_product_user (order_id, product_id, user_id),
  KEY idx_review_product (product_id),
  KEY idx_review_user (user_id),
  KEY idx_review_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE add_index_if_missing;

INSERT INTO `user` (id, username, password, nickname, phone, student_no, campus, college, dormitory, credit_score, deal_count, role, status)
VALUES
  (1, 'admin', '123456', '任亚浩', '13800000000', '20260001', '明向校区', '计算机科学与技术学院', '后台管理', 100, 0, 'ADMIN', 'ENABLED'),
  (2, 'user', '123456', '王嘉毅', '13900000000', '20260002', '明向校区', '软件学院', '清泽苑 3 号楼', 96, 3, 'USER', 'ENABLED')
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
  (11, 1, '手机电脑', 11, 'ON'),
  (12, 1, '耳机相机', 12, 'ON'),
  (21, 2, '考研教材', 21, 'ON'),
  (22, 2, '专业课资料', 22, 'ON'),
  (31, 3, '小家电', 31, 'ON'),
  (32, 3, '收纳日用', 32, 'ON'),
  (41, 4, '球拍护具', 41, 'ON'),
  (42, 4, '自行车滑板', 42, 'ON')
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT INTO product
  (id, category_id, seller_id, name, description, price, original_price, stock, cover_image, sales, status,
   condition_level, campus, trade_place, trade_type, audit_status, item_status, view_count, favorite_count, reject_reason)
VALUES
  (1, 11, 2, '九成新 ThinkPad X1 Carbon', '大三课程设计期间主要用来写代码，键盘手感好，电池正常，附原装充电器。', 2680.00, 8999.00, 1, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853', 0, 'ON', '九成新', '明向校区', '行知楼 B 座门口', '线下面交', 'APPROVED', 'ON_SALE', 138, 12, NULL),
  (2, 11, 2, 'iPad Air 5 64G 深空灰', '屏幕无划痕，平时只看网课和做笔记，可附送保护壳。', 2799.00, 4399.00, 1, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0', 0, 'ON', '九成新', '迎西校区', '图书馆一层大厅', '线下面交', 'APPROVED', 'ON_SALE', 91, 9, NULL),
  (3, 21, 2, '高等数学同济第七版上下册', '书角有轻微折痕，内页笔记不多，适合补课和期末复习。', 28.00, 86.00, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794', 0, 'ON', '八成新', '明向校区', '清泽餐厅门口', '线下面交', 'APPROVED', 'ON_SALE', 64, 7, NULL),
  (4, 22, 2, '数据结构课程设计参考资料', '包含往年实验整理和自己整理的思维导图，纸质打印版。', 18.00, 45.00, 1, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3', 0, 'ON', '七成新', '明向校区', '计算机学院楼下', '线下面交', 'APPROVED', 'ON_SALE', 56, 4, NULL),
  (5, 31, 2, '宿舍小冰箱 46L', '毕业搬宿舍出，制冷正常，适合放饮料和水果，需要自提。', 220.00, 599.00, 1, 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5', 0, 'ON', '八成新', '明向校区', '清泽苑 3 号楼下', '线下面交', 'APPROVED', 'ON_SALE', 112, 15, NULL),
  (6, 32, 2, '桌面收纳架三层', '铁艺三层架，放教材和键盘都可以，宿舍桌面更整齐。', 25.00, 79.00, 1, 'https://images.unsplash.com/photo-1497366754035-f200968a6e72', 0, 'ON', '九成新', '虎峪校区', '致明楼门口', '线下面交', 'APPROVED', 'ON_SALE', 43, 3, NULL),
  (7, 41, 2, 'Yonex 羽毛球拍', '轻量拍，拍线刚换不久，适合日常打球。', 120.00, 399.00, 1, 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea', 0, 'ON', '八成新', '明向校区', '体育馆北门', '线下面交', 'APPROVED', 'ON_SALE', 76, 8, NULL),
  (8, 42, 2, '捷安特山地车', '通勤骑行正常，刹车已调，适合明向校区内代步。', 480.00, 1599.00, 1, 'https://images.unsplash.com/photo-1485965120184-e220f721d03e', 0, 'ON', '七成新', '明向校区', '西门停车区', '线下面交', 'APPROVED', 'ON_SALE', 183, 21, NULL),
  (9, 12, 2, '索尼降噪耳机 WH-1000XM4', '耳罩轻微使用痕迹，降噪正常，支持现场试机。', 780.00, 2299.00, 1, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', 0, 'ON', '九成新', '迎西校区', '主楼前广场', '线下面交', 'APPROVED', 'ON_SALE', 127, 16, NULL)
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

INSERT INTO product_message (id, product_id, user_id, content, reply_content, status)
VALUES
  (1, 1, 2, '电脑电池续航现在大概能撑多久？可以在行知楼当面试一下吗？', '续航大概 4 小时，可以在行知楼门口试机。', 'ON'),
  (2, 5, 2, '小冰箱需要自己搬吗？晚上方便看货吗？', NULL, 'ON')
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  reply_content = VALUES(reply_content),
  status = VALUES(status);
