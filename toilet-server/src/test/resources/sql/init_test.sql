-- ========== 测试数据库初始化脚本 (H2 MySQL兼容模式) ==========

DROP TABLE IF EXISTS tb_message;
DROP TABLE IF EXISTS tb_feedback;
DROP TABLE IF EXISTS tb_consumable_record;
DROP TABLE IF EXISTS tb_consumable_stock;
DROP TABLE IF EXISTS tb_clean_schedule;
DROP TABLE IF EXISTS tb_repair_order;
DROP TABLE IF EXISTS tb_facility;
DROP TABLE IF EXISTS tb_user;
DROP TABLE IF EXISTS tb_toilet;

-- 1. 公厕基础信息表
CREATE TABLE tb_toilet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_code VARCHAR(20) NOT NULL COMMENT '公厕编码',
    name VARCHAR(100) NOT NULL COMMENT '公厕名称',
    address VARCHAR(200) COMMENT '详细地址',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    district VARCHAR(50) COMMENT '所属区域',
    open_time VARCHAR(50) COMMENT '开放时间',
    male_count INT DEFAULT 0 COMMENT '男厕位数',
    female_count INT DEFAULT 0 COMMENT '女厕位数',
    accessible_count INT DEFAULT 0 COMMENT '无障碍厕位数',
    manage_unit VARCHAR(100) COMMENT '管理单位',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='公厕基础信息表';

-- 2. 用户表
CREATE TABLE tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    role VARCHAR(20) NOT NULL COMMENT '角色：ADMIN/CLEANER/REPAIR',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='用户表';

-- 3. 设施台账表
CREATE TABLE tb_facility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_id BIGINT NOT NULL COMMENT '关联公厕ID',
    facility_type VARCHAR(50) NOT NULL COMMENT '设施类型（洗手台/马桶/小便池等）',
    brand VARCHAR(50) COMMENT '品牌',
    model VARCHAR(50) COMMENT '型号',
    install_date DATE COMMENT '安装日期',
    warranty_date DATE COMMENT '保修截止日期',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态（NORMAL正常/FAULT故障/REPAIR维修中）',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='设施台账表';

-- 4. 报修工单表
CREATE TABLE tb_repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(30) NOT NULL COMMENT '工单编号',
    toilet_id BIGINT NOT NULL COMMENT '关联公厕ID',
    facility_id BIGINT NOT NULL COMMENT '关联设施ID',
    fault_desc VARCHAR(500) NOT NULL COMMENT '故障描述',
    reporter_id BIGINT NOT NULL COMMENT '报修人ID',
    assignee_id BIGINT COMMENT '维修人ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待处理/REPAIRING维修中/CHECKING待验收/FINISHED已完成/CANCELLED已取消',
    repair_desc VARCHAR(500) COMMENT '维修描述',
    images VARCHAR(500) COMMENT '故障图片（逗号分隔）',
    reporter_name VARCHAR(50) COMMENT '上报人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time DATETIME COMMENT '完成时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='报修工单表';

-- 5. 保洁排班表
CREATE TABLE tb_clean_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_id BIGINT NOT NULL COMMENT '关联公厕ID',
    cleaner_id BIGINT NOT NULL COMMENT '保洁员ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    shift_type VARCHAR(20) NOT NULL COMMENT '班次：MORNING上午 AFTERNOON下午',
    clean_type VARCHAR(20) NOT NULL COMMENT '保洁类型：DAILY日常 DEEP深度',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待打卡 CHECKED_IN已签到 SIGNED_OFF已签退',
    checkin_time DATETIME COMMENT '签到时间',
    signoff_time DATETIME COMMENT '签退时间',
    score INT COMMENT '评分（0-100）',
    remark VARCHAR(200) COMMENT '备注/评价',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='保洁排班表';

-- 6. 耗材库存表
CREATE TABLE tb_consumable_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_id BIGINT NOT NULL COMMENT '关联公厕ID',
    consumable_name VARCHAR(100) NOT NULL COMMENT '耗材名称',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    current_stock INT DEFAULT 0 COMMENT '当前库存量',
    min_stock INT DEFAULT 10 COMMENT '最低库存预警值',
    update_time DATETIME COMMENT '最后更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='耗材库存表';

-- 7. 耗材出入库记录表
CREATE TABLE tb_consumable_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_id BIGINT NOT NULL COMMENT '关联公厕ID',
    consumable_name VARCHAR(100) NOT NULL COMMENT '耗材名称',
    quantity INT NOT NULL COMMENT '出入库数量',
    type VARCHAR(10) NOT NULL COMMENT '类型：IN入库 OUT出库',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='耗材出入库记录表';

-- 8. 公众反馈表
CREATE TABLE tb_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    toilet_id BIGINT COMMENT '关联公厕ID',
    user_id BIGINT COMMENT '用户ID',
    score INT NOT NULL COMMENT '评分（1-5分）',
    content VARCHAR(500) COMMENT '反馈内容',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='公众反馈表';

-- 9. 站内消息表
CREATE TABLE tb_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content VARCHAR(500) COMMENT '消息内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
) COMMENT='站内消息表';

-- ========== 测试种子数据 ==========

-- 用户（管理员 + 保洁 + 维修）
INSERT INTO tb_user (id, username, password, real_name, phone, role, status) VALUES
(1, 'admin', '$2a$10$NZ5o7r2E8XOo/RRfL1F4aOkuDq7JwYMpGdSopHU5LdKjmkKf3FVrq', '系统管理员', '13800000000', 'ADMIN', 1);
INSERT INTO tb_user (username, password, real_name, phone, role, status) VALUES
('cleaner1', '$2a$10$NZ5o7r2E8XOo/RRfL1F4aOkuDq7JwYMpGdSopHU5LdKjmkKf3FVrq', '张保洁', '13800000001', 'CLEANER', 1);
INSERT INTO tb_user (username, password, real_name, phone, role, status) VALUES
('repair1', '$2a$10$NZ5o7r2E8XOo/RRfL1F4aOkuDq7JwYMpGdSopHU5LdKjmkKf3FVrq', '王维修', '13800000003', 'REPAIR', 1);

-- 公厕
INSERT INTO tb_toilet (id, toilet_code, name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, status) VALUES
(1, 'TC001', '测试公厕A', '测试地址A', 116.397428, 39.909200, '测试区', '06:00-22:00', 8, 10, 2, 1);

-- 设施
INSERT INTO tb_facility (id, toilet_id, facility_type, brand, model, install_date, status) VALUES
(1, 1, '洗手台', '九牧', 'JM-200A', '2024-01-15', 'NORMAL');

-- 保洁排班（ID 1: PENDING 用于打卡测试；ID 2: SIGNED_OFF 用于评分测试）
INSERT INTO tb_clean_schedule (id, toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time) VALUES
(1, 1, 1, CURRENT_DATE, 'MORNING', 'DAILY', 'PENDING', NOW());
INSERT INTO tb_clean_schedule (id, toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, create_time) VALUES
(2, 1, 1, CURRENT_DATE - 1, 'MORNING', 'DAILY', 'SIGNED_OFF', NOW() - 1, NOW() - 1, NOW() - 1);

-- 维修工单（PENDING 状态）
INSERT INTO tb_repair_order (id, order_no, toilet_id, facility_id, fault_desc, reporter_id, status, create_time) VALUES
(1, 'RO-TEST-001', 1, 1, '测试故障描述', 1, 'PENDING', NOW());

-- ========== 补充种子数据（AuthService / ConsumableService / FacilityService / DashboardService）==========

-- 被禁用的用户（用于 AuthService 禁用账号测试）
INSERT INTO tb_user (id, username, password, real_name, phone, role, status) VALUES
(4, 'disabled', '$2a$10$NZ5o7r2E8XOo/RRfL1F4aOkuDq7JwYMpGdSopHU5LdKjmkKf3FVrq', '已禁用用户', '13800000004', 'CLEANER', 0);

-- 第二个公厕（用于多公厕 Dashboard 图表测试）
INSERT INTO tb_toilet (id, toilet_code, name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, status) VALUES
(2, 'TC002', '测试公厕B', '测试地址B', 116.500000, 39.800000, '测试区', '07:00-21:00', 6, 8, 1, 1);

-- 设施：第二个正常 + 一个故障设施（用于 Dashboard 设施完好率测试）
INSERT INTO tb_facility (id, toilet_id, facility_type, brand, model, install_date, status) VALUES
(2, 1, '马桶', 'TOTO', 'CW988', '2024-03-01', 'BROKEN');
INSERT INTO tb_facility (id, toilet_id, facility_type, brand, model, install_date, status) VALUES
(3, 2, '洗手台', '箭牌', 'AL-101', '2024-06-01', 'NORMAL');

-- 耗材库存（用于 ConsumableService 出库测试 + Dashboard 低库存告警）
INSERT INTO tb_consumable_stock (id, toilet_id, consumable_name, unit, current_stock, min_stock, update_time) VALUES
(1, 1, '卫生纸', '卷', 50, 10, NOW());
INSERT INTO tb_consumable_stock (id, toilet_id, consumable_name, unit, current_stock, min_stock, update_time) VALUES
(2, 1, '洗手液', '瓶', 5, 10, NOW());

-- 耗材出库记录（用于 Dashboard 耗材使用量图表）
INSERT INTO tb_consumable_record (id, toilet_id, consumable_name, quantity, type, operator_id, create_time) VALUES
(1, 1, '卫生纸', 10, 'OUT', 1, NOW() - 5);

-- 更多反馈记录（用于 Dashboard 满意度统计）
INSERT INTO tb_feedback (id, toilet_id, user_id, score, content, submit_time) VALUES
(1, 1, 1, 5, '非常好', NOW() - 10);
INSERT INTO tb_feedback (id, toilet_id, user_id, score, content, submit_time) VALUES
(2, 1, 2, 4, '还不错', NOW() - 8);
INSERT INTO tb_feedback (id, toilet_id, user_id, score, content, submit_time) VALUES
(3, 2, 1, 3, '一般', NOW() - 6);
INSERT INTO tb_feedback (id, toilet_id, user_id, score, content, submit_time) VALUES
(4, 1, 3, 2, '需改进', NOW() - 3);

-- 带评分的保洁排班（用于 Dashboard 清洁合格率图表）
INSERT INTO tb_clean_schedule (id, toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, score, create_time) VALUES
(3, 1, 1, CURRENT_DATE - 1, 'AFTERNOON', 'DAILY', 'SIGNED_OFF', 85, NOW() - 1);
INSERT INTO tb_clean_schedule (id, toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, score, create_time) VALUES
(4, 2, 1, CURRENT_DATE - 2, 'MORNING', 'DAILY', 'SIGNED_OFF', 45, NOW() - 2);
