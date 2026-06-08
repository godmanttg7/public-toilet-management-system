-- ============================================
-- 公厕管理系统 - 达梦数据库初始化脚本
-- 使用方法: 在达梦管理工具中执行此脚本
--
-- 前提: 已创建 toilet_db 用户/模式
-- CREATE USER toilet_db IDENTIFIED BY "Toilet123456";
-- GRANT DBA TO toilet_db;
-- ============================================

-- 切换到 toilet_db 模式（达梦中用户=模式）
SET SCHEMA toilet_db;

-- ========== 按依赖顺序逆序删除（先删子表，避免外键冲突） ==========
DROP TABLE IF EXISTS tb_incident_report CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_message CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_feedback CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_consumable_record CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_consumable_stock CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_clean_schedule CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_repair_order CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_facility CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_user CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS tb_toilet CASCADE CONSTRAINTS;

-- ========== 1. 公厕基础信息表 ==========
-- 注意: 实体类 Toilet.java 中无 toiletCode 字段，故不创建 toilet_code 列
CREATE TABLE tb_toilet (
    id BIGINT NOT NULL IDENTITY(1,1),
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    district VARCHAR(50),
    open_time VARCHAR(50),
    male_count INT DEFAULT 0,
    female_count INT DEFAULT 0,
    accessible_count INT DEFAULT 0,
    manage_unit VARCHAR(100),
    phone VARCHAR(20),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_toilet IS '公厕基础信息表';
COMMENT ON COLUMN tb_toilet.id IS '公厕编号';
COMMENT ON COLUMN tb_toilet.name IS '公厕名称';
COMMENT ON COLUMN tb_toilet.address IS '详细地址';
COMMENT ON COLUMN tb_toilet.longitude IS '经度';
COMMENT ON COLUMN tb_toilet.latitude IS '纬度';
COMMENT ON COLUMN tb_toilet.district IS '所属区域';
COMMENT ON COLUMN tb_toilet.open_time IS '开放时间';
COMMENT ON COLUMN tb_toilet.male_count IS '男厕位数量';
COMMENT ON COLUMN tb_toilet.female_count IS '女厕位数量';
COMMENT ON COLUMN tb_toilet.accessible_count IS '无障碍厕位数量';
COMMENT ON COLUMN tb_toilet.manage_unit IS '管理单位';
COMMENT ON COLUMN tb_toilet.phone IS '联系电话';
COMMENT ON COLUMN tb_toilet.status IS '状态（1正常/0停用）';
COMMENT ON COLUMN tb_toilet.create_time IS '创建时间';
COMMENT ON COLUMN tb_toilet.update_time IS '更新时间';
COMMENT ON COLUMN tb_toilet.deleted IS '逻辑删除（0未删除/1已删除）';

-- ========== 2. 用户表 ==========
CREATE TABLE tb_user (
    id BIGINT NOT NULL IDENTITY(1,1),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_user IS '用户表';
COMMENT ON COLUMN tb_user.id IS '用户编号';
COMMENT ON COLUMN tb_user.username IS '用户名';
COMMENT ON COLUMN tb_user.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN tb_user.real_name IS '真实姓名';
COMMENT ON COLUMN tb_user.phone IS '联系电话';
COMMENT ON COLUMN tb_user.role IS '角色（ADMIN管理员/CLEANER保洁员/REPAIR维修工/PUBLIC公众用户）';
COMMENT ON COLUMN tb_user.status IS '状态（1启用/0禁用）';
COMMENT ON COLUMN tb_user.create_time IS '创建时间';
COMMENT ON COLUMN tb_user.update_time IS '更新时间';
COMMENT ON COLUMN tb_user.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE UNIQUE INDEX uk_username ON tb_user(username);

-- ========== 3. 设施台账表 ==========
CREATE TABLE tb_facility (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT NOT NULL,
    facility_type VARCHAR(50) NOT NULL,
    brand VARCHAR(50),
    "model" VARCHAR(50),
    install_date DATE,
    warranty_date DATE,
    status VARCHAR(20) DEFAULT 'NORMAL',
    version INT DEFAULT 0 NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_facility IS '设施台账表';
COMMENT ON COLUMN tb_facility.id IS '设施编号';
COMMENT ON COLUMN tb_facility.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_facility.facility_type IS '设施类型';
COMMENT ON COLUMN tb_facility.brand IS '品牌';
COMMENT ON COLUMN tb_facility."model" IS '型号';
COMMENT ON COLUMN tb_facility.install_date IS '安装日期';
COMMENT ON COLUMN tb_facility.warranty_date IS '保修到期日期';
COMMENT ON COLUMN tb_facility.status IS '状态（NORMAL正常/FAULT故障/REPAIR维修中）';
COMMENT ON COLUMN tb_facility.create_time IS '创建时间';
COMMENT ON COLUMN tb_facility.update_time IS '更新时间';
COMMENT ON COLUMN tb_facility.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_facility_toilet ON tb_facility(toilet_id);

-- ========== 4. 报修工单表 ==========
CREATE TABLE tb_repair_order (
    id BIGINT NOT NULL IDENTITY(1,1),
    order_no VARCHAR(30) NOT NULL,
    toilet_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    fault_desc VARCHAR(500) NOT NULL,
    reporter_id BIGINT NOT NULL,
    reporter_name VARCHAR(50),
    assignee_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    repair_desc VARCHAR(500),
    images VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    finish_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_repair_order IS '报修工单表';
COMMENT ON COLUMN tb_repair_order.id IS '工单编号';
COMMENT ON COLUMN tb_repair_order.order_no IS '工单业务编号';
COMMENT ON COLUMN tb_repair_order.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_repair_order.facility_id IS '关联设施';
COMMENT ON COLUMN tb_repair_order.fault_desc IS '故障描述';
COMMENT ON COLUMN tb_repair_order.reporter_id IS '上报人ID';
COMMENT ON COLUMN tb_repair_order.reporter_name IS '上报人姓名（手动填写）';
COMMENT ON COLUMN tb_repair_order.assignee_id IS '指派维修人';
COMMENT ON COLUMN tb_repair_order.status IS '工单状态（PENDING待处理/REPAIRING维修中/CHECKING待验收/FINISHED已完成/CANCELLED已取消）';
COMMENT ON COLUMN tb_repair_order.repair_desc IS '维修记录';
COMMENT ON COLUMN tb_repair_order.images IS '故障图片（逗号分隔）';
COMMENT ON COLUMN tb_repair_order.create_time IS '创建时间';
COMMENT ON COLUMN tb_repair_order.finish_time IS '完成时间';
COMMENT ON COLUMN tb_repair_order.update_time IS '更新时间';
COMMENT ON COLUMN tb_repair_order.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE UNIQUE INDEX uk_order_no ON tb_repair_order(order_no);
CREATE INDEX idx_repair_toilet ON tb_repair_order(toilet_id);
CREATE INDEX idx_repair_facility ON tb_repair_order(facility_id);

-- ========== 5. 保洁排班表 ==========
CREATE TABLE tb_clean_schedule (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT NOT NULL,
    cleaner_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    clean_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    checkin_time TIMESTAMP,
    signoff_time TIMESTAMP,
    score INT,
    remark VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_clean_schedule IS '保洁排班表';
COMMENT ON COLUMN tb_clean_schedule.id IS '排班编号';
COMMENT ON COLUMN tb_clean_schedule.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_clean_schedule.cleaner_id IS '保洁人员';
COMMENT ON COLUMN tb_clean_schedule.schedule_date IS '排班日期';
COMMENT ON COLUMN tb_clean_schedule.shift_type IS '班次类型（MORNING/AFTERNOON/EVENING）';
COMMENT ON COLUMN tb_clean_schedule.clean_type IS '保洁类型（DAILY/DEEP）';
COMMENT ON COLUMN tb_clean_schedule.status IS '状态（PENDING待打卡/CHECKED_IN已打卡/SIGNED_OFF已签退/REJECTED整改中/FINISHED已完成）';
COMMENT ON COLUMN tb_clean_schedule.checkin_time IS '打卡时间';
COMMENT ON COLUMN tb_clean_schedule.signoff_time IS '签退时间';
COMMENT ON COLUMN tb_clean_schedule.score IS '保洁质量评分（0-100）';
COMMENT ON COLUMN tb_clean_schedule.remark IS '评价备注';
COMMENT ON COLUMN tb_clean_schedule.create_time IS '创建时间';
COMMENT ON COLUMN tb_clean_schedule.update_time IS '更新时间';
COMMENT ON COLUMN tb_clean_schedule.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_schedule_toilet ON tb_clean_schedule(toilet_id);
CREATE INDEX idx_schedule_cleaner ON tb_clean_schedule(cleaner_id);

-- ========== 6. 耗材库存表 ==========
-- 注意: 实体类 ConsumableStock.java 中无 createTime 字段，故不创建 create_time 列
CREATE TABLE tb_consumable_stock (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT NOT NULL,
    consumable_name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    current_stock INT DEFAULT 0,
    min_stock INT DEFAULT 10,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_consumable_stock IS '耗材库存表';
COMMENT ON COLUMN tb_consumable_stock.id IS '记录编号';
COMMENT ON COLUMN tb_consumable_stock.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_consumable_stock.consumable_name IS '耗材名称';
COMMENT ON COLUMN tb_consumable_stock.unit IS '计量单位';
COMMENT ON COLUMN tb_consumable_stock.current_stock IS '当前库存';
COMMENT ON COLUMN tb_consumable_stock.min_stock IS '库存下限';
COMMENT ON COLUMN tb_consumable_stock.update_time IS '最后更新时间';
COMMENT ON COLUMN tb_consumable_stock.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_stock_toilet ON tb_consumable_stock(toilet_id);

-- ========== 7. 耗材出入库记录表 ==========
CREATE TABLE tb_consumable_record (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT NOT NULL,
    consumable_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    operator_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_consumable_record IS '耗材出入库记录表';
COMMENT ON COLUMN tb_consumable_record.id IS '记录编号';
COMMENT ON COLUMN tb_consumable_record.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_consumable_record.consumable_name IS '耗材名称';
COMMENT ON COLUMN tb_consumable_record.quantity IS '数量';
COMMENT ON COLUMN tb_consumable_record.type IS '类型（IN入库/OUT出库）';
COMMENT ON COLUMN tb_consumable_record.operator_id IS '操作人';
COMMENT ON COLUMN tb_consumable_record.create_time IS '操作时间';
COMMENT ON COLUMN tb_consumable_record.update_time IS '更新时间';
COMMENT ON COLUMN tb_consumable_record.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_record_toilet ON tb_consumable_record(toilet_id);

-- ========== 8. 公众反馈表 ==========
CREATE TABLE tb_feedback (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT,
    user_id BIGINT,
    score INT NOT NULL,
    content VARCHAR(500),
    submit_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_feedback IS '公众反馈表';
COMMENT ON COLUMN tb_feedback.id IS '反馈编号';
COMMENT ON COLUMN tb_feedback.toilet_id IS '所属公厕（公众展示台反馈可为空）';
COMMENT ON COLUMN tb_feedback.user_id IS '用户ID（公众展示台反馈可为空）';
COMMENT ON COLUMN tb_feedback.score IS '满意度评分（1-5）';
COMMENT ON COLUMN tb_feedback.content IS '意见内容';
COMMENT ON COLUMN tb_feedback.submit_time IS '提交时间';
COMMENT ON COLUMN tb_feedback.update_time IS '更新时间';
COMMENT ON COLUMN tb_feedback.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_feedback_toilet ON tb_feedback(toilet_id);

-- ========== 9. 站内消息表 ==========
CREATE TABLE tb_message (
    id BIGINT NOT NULL IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500),
    is_read SMALLINT DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_message IS '站内消息表';
COMMENT ON COLUMN tb_message.id IS '消息编号';
COMMENT ON COLUMN tb_message.user_id IS '接收用户';
COMMENT ON COLUMN tb_message.title IS '消息标题';
COMMENT ON COLUMN tb_message.content IS '消息内容';
COMMENT ON COLUMN tb_message.is_read IS '是否已读（1已读/0未读）';
COMMENT ON COLUMN tb_message.create_time IS '创建时间';
COMMENT ON COLUMN tb_message.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_message_user ON tb_message(user_id);

-- ========== 10. 情况汇报/事件上报表 ==========
CREATE TABLE tb_incident_report (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT,
    reporter_id BIGINT,
    reporter_role VARCHAR(20),
    report_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    images VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_incident_report IS '情况汇报/事件上报表';
COMMENT ON COLUMN tb_incident_report.id IS '编号';
COMMENT ON COLUMN tb_incident_report.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_incident_report.reporter_id IS '上报人';
COMMENT ON COLUMN tb_incident_report.reporter_role IS '上报人角色';
COMMENT ON COLUMN tb_incident_report.report_type IS '汇报类型';
COMMENT ON COLUMN tb_incident_report.description IS '描述内容';
COMMENT ON COLUMN tb_incident_report.images IS '图片（逗号分隔）';
COMMENT ON COLUMN tb_incident_report.status IS '状态（PENDING待处理/PROCESSING处理中/RESOLVED已解决）';
COMMENT ON COLUMN tb_incident_report.create_time IS '创建时间';
COMMENT ON COLUMN tb_incident_report.update_time IS '更新时间';
COMMENT ON COLUMN tb_incident_report.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_incident_toilet ON tb_incident_report(toilet_id);
CREATE INDEX idx_incident_reporter ON tb_incident_report(reporter_id);

-- ========== 外键约束（达梦支持 ALTER TABLE ADD CONSTRAINT） ==========
ALTER TABLE tb_facility ADD CONSTRAINT fk_facility_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);

ALTER TABLE tb_repair_order ADD CONSTRAINT fk_repair_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);
ALTER TABLE tb_repair_order ADD CONSTRAINT fk_repair_facility FOREIGN KEY (facility_id) REFERENCES tb_facility(id);
ALTER TABLE tb_repair_order ADD CONSTRAINT fk_repair_reporter FOREIGN KEY (reporter_id) REFERENCES tb_user(id);
ALTER TABLE tb_repair_order ADD CONSTRAINT fk_repair_assignee FOREIGN KEY (assignee_id) REFERENCES tb_user(id);

ALTER TABLE tb_clean_schedule ADD CONSTRAINT fk_schedule_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);
ALTER TABLE tb_clean_schedule ADD CONSTRAINT fk_schedule_cleaner FOREIGN KEY (cleaner_id) REFERENCES tb_user(id);

ALTER TABLE tb_consumable_stock ADD CONSTRAINT fk_stock_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);

ALTER TABLE tb_consumable_record ADD CONSTRAINT fk_record_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);
ALTER TABLE tb_consumable_record ADD CONSTRAINT fk_record_operator FOREIGN KEY (operator_id) REFERENCES tb_user(id);

ALTER TABLE tb_feedback ADD CONSTRAINT fk_feedback_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);
ALTER TABLE tb_feedback ADD CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES tb_user(id);

ALTER TABLE tb_message ADD CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES tb_user(id);

ALTER TABLE tb_incident_report ADD CONSTRAINT fk_incident_toilet FOREIGN KEY (toilet_id) REFERENCES tb_toilet(id);
ALTER TABLE tb_incident_report ADD CONSTRAINT fk_incident_reporter FOREIGN KEY (reporter_id) REFERENCES tb_user(id);

-- ================================================================
-- 初始化种子数据（使用 WHERE NOT EXISTS 防止重复插入）
-- 密码说明: 所有用户密码均为 123456（BCrypt加密）
-- 数据库密码: Toilet123456（application.yml 中配置）
-- ================================================================

-- ========== 用户数据 ==========
INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'admin', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '系统管理员', '13800000000', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'admin');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'cleaner1', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '张保洁', '13800000001', 'CLEANER', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'cleaner1');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'cleaner2', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '李保洁', '13800000002', 'CLEANER', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'cleaner2');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'repair1', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '王维修', '13800000003', 'REPAIR', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'repair1');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'repair2', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '赵维修', '13800000004', 'REPAIR', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'repair2');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'cleaner3', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '刘保洁', '13800000005', 'CLEANER', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'cleaner3');

INSERT INTO tb_user (username, password, real_name, phone, role, status)
SELECT 'public1', '$2a$10$5/ciuQstCo6giLnpoSNmbO9CTK4pj111aVRE/DosLTWXx0.kR5ufK', '普通用户', '13900000001', 'PUBLIC', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username = 'public1');

-- ========== 公厕数据 ==========
INSERT INTO tb_toilet (name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, manage_unit, phone, status)
SELECT '人民广场公厕', 'XX市人民广场东侧50米', 116.397428, 39.909200, '市中心区', '06:00-22:00', 8, 10, 2, '市环卫局', '010-12345678', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_toilet WHERE name = '人民广场公厕');

INSERT INTO tb_toilet (name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, manage_unit, phone, status)
SELECT '火车站公交车站公厕', 'XX市火车站北广场公交枢纽站内', 116.421000, 39.902500, '火车站片区', '05:00-23:00', 12, 15, 3, '市环卫局', '010-12345679', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_toilet WHERE name = '火车站公交车站公厕');

INSERT INTO tb_toilet (name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, manage_unit, phone, status)
SELECT '朝阳公园北门公厕', 'XX市朝阳公园北门入口西侧', 116.405200, 39.915800, '朝阳区', '06:00-21:00', 6, 8, 2, '朝阳区环卫中心', '010-12345680', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_toilet WHERE name = '朝阳公园北门公厕');

INSERT INTO tb_toilet (name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, manage_unit, phone, status)
SELECT '滨河大道西段公厕', 'XX市滨河大道西段88号路灯旁', 116.385400, 39.920100, '滨河区', '00:00-24:00', 4, 6, 1, '滨河区环卫中心', '010-12345681', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_toilet WHERE name = '滨河大道西段公厕');

INSERT INTO tb_toilet (name, address, longitude, latitude, district, open_time, male_count, female_count, accessible_count, manage_unit, phone, status)
SELECT '老街步行街中心公厕', 'XX市老街步行街中段102号对面', 116.410600, 39.908300, '老城区', '08:00-21:00', 5, 7, 1, '老城区环卫中心', '010-12345682', 1
WHERE NOT EXISTS (SELECT 1 FROM tb_toilet WHERE name = '老街步行街中心公厕');

-- ========== 设施数据（每个公厕3-5个设施，混合NORMAL/FAULT/REPAIR状态） =========--

-- 公厕1: 人民广场 — 5个设施
INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手台', '九牧', 'JM-200A', '2024-01-15', '2027-01-15', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility f JOIN tb_toilet t ON f.toilet_id = t.id WHERE t.name = '人民广场公厕' AND f.facility_type = '洗手台' AND f.brand = '九牧');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '烘手机', '松下', 'FJ-100', '2024-01-15', '2026-01-15', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '烘手机');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '通风设备', '格力', 'XJ-300', '2024-03-20', '2026-03-20', 'REPAIR'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '通风设备');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '小便器', 'TOTO', 'CW-700', '2024-01-15', '2027-01-15', 'REPAIR'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '小便器' AND brand = 'TOTO');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '照明设备', '欧普', 'OP-LED20', '2024-01-15', '2026-01-15', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '照明设备');

-- 公厕2: 火车站 — 5个设施（含2个故障/维修中）
INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '洗手台', '九牧', 'JM-200A', '2024-02-10', '2027-02-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '洗手台');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '烘手机', '松下', 'FJ-100', '2024-02-10', '2026-02-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '烘手机');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '座便器', 'TOTO', 'CW-850', '2024-02-10', '2027-02-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '座便器');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '通风设备', '美的', 'FV-300', '2024-02-10', '2026-02-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '通风设备' AND brand = '美的');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '自动冲水器', '九牧', 'CS-100', '2024-03-01', '2027-03-01', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '自动冲水器');

-- 公厕3: 朝阳公园 — 4个设施
INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '洗手台', '九牧', 'JM-300B', '2024-05-10', '2027-05-10', 'REPAIR'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '洗手台');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '小便器', 'TOTO', 'CW-800', '2024-05-10', '2027-05-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '小便器');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '通风设备', '美的', 'FV-200', '2024-06-15', '2026-06-15', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '通风设备' AND brand = '美的');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '烘手机', '松下', 'FJ-200', '2024-05-10', '2026-05-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '烘手机');

-- 公厕4: 滨河大道 — 4个设施（含1个待维修）
INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '洗手台', '箭牌', 'JW-100', '2024-07-20', '2027-07-20', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '洗手台');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '座便器', 'TOTO', 'CW-900', '2024-07-20', '2027-07-20', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '座便器');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '照明设备', '欧普', 'OP-LED', '2024-07-20', '2026-07-20', 'FAULT'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '照明设备');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '排气扇', '格力', 'PF-100', '2024-07-20', '2026-07-20', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '排气扇');

-- 公厕5: 老街步行街 — 4个设施
INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '洗手台', '九牧', 'JM-200A', '2024-08-10', '2027-08-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND facility_type = '洗手台' AND brand = '九牧');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '烘手机', '松下', 'FJ-200', '2024-08-10', '2026-08-10', 'FAULT'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND facility_type = '烘手机');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '坐便器', '箭牌', 'JW-200', '2024-08-10', '2027-08-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND facility_type = '坐便器');

INSERT INTO tb_facility (toilet_id, facility_type, brand, "model", install_date, warranty_date, status)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '洗手台', '箭牌', 'JW-101', '2024-08-10', '2027-08-10', 'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND facility_type = '洗手台' AND brand = '箭牌');

-- ========== 报修工单数据（10条，覆盖各种状态） ==========
INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time, finish_time)
SELECT 'RO20260501001', (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '洗手台' AND brand = '九牧'), '洗手台水龙头漏水，关不紧', (SELECT id FROM tb_user WHERE username = 'admin'), (SELECT id FROM tb_user WHERE username = 'repair1'), 'FINISHED', '更换水龙头密封圈，已修复', SYSDATE - 22, SYSDATE - 21
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260501001');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time, finish_time)
SELECT 'RO20260510002', (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '烘手机' AND brand = '松下'), '烘手机不工作了，按下没有反应', (SELECT id FROM tb_user WHERE username = 'cleaner1'), (SELECT id FROM tb_user WHERE username = 'repair2'), 'FINISHED', '电机损坏，更换新烘手机', SYSDATE - 14, SYSDATE - 12
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260510002');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time)
SELECT 'RO20260515003', (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '通风设备' AND brand = '格力'), '排风扇噪音很大，有异响', (SELECT id FROM tb_user WHERE username = 'admin'), (SELECT id FROM tb_user WHERE username = 'repair1'), 'CHECKING', '清洗风扇叶片并添加润滑油，请验收', SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260515003');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, create_time)
SELECT 'RO20260520004', (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '洗手台' AND brand = '九牧'), '洗手台下水道堵塞，排水缓慢', (SELECT id FROM tb_user WHERE username = 'cleaner2'), (SELECT id FROM tb_user WHERE username = 'repair1'), 'REPAIRING', SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260520004');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, status, create_time)
SELECT 'RO20260524005', (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '照明设备' AND brand = '欧普'), '照明灯闪烁，可能接触不良', (SELECT id FROM tb_user WHERE username = 'admin'), 'PENDING', SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260524005');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, status, create_time)
SELECT 'RO20260525006', (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND facility_type = '烘手机' AND brand = '松下'), '烘手机吹风不热，加热模块故障', (SELECT id FROM tb_user WHERE username = 'cleaner2'), 'PENDING', SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260525006');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time, finish_time)
SELECT 'RO20260518007', (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND facility_type = '座便器' AND brand = 'TOTO'), '座便器冲水按钮卡住', (SELECT id FROM tb_user WHERE username = 'cleaner1'), (SELECT id FROM tb_user WHERE username = 'repair2'), 'FINISHED', '更换冲水按钮弹簧，已恢复正常', SYSDATE - 11, SYSDATE - 10
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260518007');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time, finish_time)
SELECT 'RO20260520008', (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND facility_type = '烘手机' AND brand = '松下'), '烘手机噪音大，风速不足', (SELECT id FROM tb_user WHERE username = 'cleaner2'), (SELECT id FROM tb_user WHERE username = 'repair1'), 'FINISHED', '清洗过滤网，调整风道', SYSDATE - 8, SYSDATE - 7
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260520008');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, create_time)
SELECT 'RO20260522009', (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND facility_type = '小便器' AND brand = 'TOTO'), '小便器感应器失灵，长流水', (SELECT id FROM tb_user WHERE username = 'admin'), (SELECT id FROM tb_user WHERE username = 'repair2'), 'REPAIRING', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260522009');

INSERT INTO tb_repair_order (order_no, toilet_id, facility_id, fault_desc, reporter_id, assignee_id, status, repair_desc, create_time, finish_time)
SELECT 'RO20260523010', (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_facility WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND facility_type = '座便器' AND brand = 'TOTO'), '座便器盖板松动', (SELECT id FROM tb_user WHERE username = 'admin'), (SELECT id FROM tb_user WHERE username = 'repair1'), 'FINISHED', '紧固螺丝，已修复', SYSDATE - 5, SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_repair_order WHERE order_no = 'RO20260523010');

-- ========== 保洁排班数据（覆盖10天，每天多个班次，含评分和备注） ==========

-- 第-10天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 10, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 10 + 0.35, SYSDATE - 10 + 0.45, 85, '日常保洁，洗手台清洁到位', SYSDATE - 10
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 10 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 10, 'AFTERNOON', 'DAILY', 'FINISHED', SYSDATE - 10 + 0.55, SYSDATE - 10 + 0.72, 88, '下午保洁完成，地面干燥', SYSDATE - 10
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 10 AND shift_type = 'AFTERNOON');

-- 第-9天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 9, 'MORNING', 'DEEP', 'FINISHED', SYSDATE - 9 + 0.33, SYSDATE - 9 + 0.47, 92, '深度保洁，地面消毒完成', SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 9 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 9, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 9 + 0.35, SYSDATE - 9 + 0.48, 90, '火车站公厕保洁达标', SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 9 AND shift_type = 'MORNING');

-- 第-8天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 8, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 8 + 0.34, SYSDATE - 8 + 0.44, 78, '保洁质量一般，便器有污渍残留', SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 8 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 8, 'AFTERNOON', 'DAILY', 'FINISHED', SYSDATE - 8 + 0.54, SYSDATE - 8 + 0.70, 76, '人流量大，保洁效果一般', SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 8 AND shift_type = 'AFTERNOON');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 8, 'EVENING', 'DAILY', 'FINISHED', SYSDATE - 8 + 0.70, SYSDATE - 8 + 0.85, 80, '晚间保洁合格', SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 8 AND shift_type = 'EVENING');

-- 第-7天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 7, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 7 + 0.32, SYSDATE - 7 + 0.46, 88, '整体良好', SYSDATE - 7
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 7 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 7, 'MORNING', 'DEEP', 'FINISHED', SYSDATE - 7 + 0.34, SYSDATE - 7 + 0.48, 88, '深度保洁完成', SYSDATE - 7
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 7 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 7, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 7 + 0.36, SYSDATE - 7 + 0.47, 82, '滨河公厕保洁达标', SYSDATE - 7
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 7 AND shift_type = 'MORNING');

-- 第-6天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 6, 'AFTERNOON', 'DAILY', 'FINISHED', SYSDATE - 6 + 0.55, SYSDATE - 6 + 0.72, 95, '非常整洁，地面光亮', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 6 AND shift_type = 'AFTERNOON');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 6, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 6 + 0.33, SYSDATE - 6 + 0.44, 85, '公园公厕，整体干净', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 6 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 6, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 6 + 0.35, SYSDATE - 6 + 0.46, 79, '步行街人流量大，洗手台有水渍', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 6 AND shift_type = 'MORNING');

-- 第-5天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 5, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 5 + 0.33, SYSDATE - 5 + 0.45, 82, '合格', SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 5 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 5, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 5 + 0.36, SYSDATE - 5 + 0.47, 81, '早班保洁完成，厕所无异味', SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 5 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 5, 'AFTERNOON', 'DAILY', 'FINISHED', SYSDATE - 5 + 0.55, SYSDATE - 5 + 0.71, 86, '下午保洁良好', SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 5 AND shift_type = 'AFTERNOON');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 5, 'AFTERNOON', 'DAILY', 'FINISHED', SYSDATE - 5 + 0.56, SYSDATE - 5 + 0.73, 73, '步行街下午人流量极大，保洁频次需增加', SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 5 AND shift_type = 'AFTERNOON');

-- 第-4天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 4, 'MORNING', 'DEEP', 'FINISHED', SYSDATE - 4 + 0.33, SYSDATE - 4 + 0.48, 93, '深度保洁，消毒彻底', SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 4 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 4, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 4 + 0.34, SYSDATE - 4 + 0.46, 84, '日常保洁合格', SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 4 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 4, 'AFTERNOON', 'DAILY', 'REJECTED', SYSDATE - 4 + 0.54, SYSDATE - 4 + 0.68, 45, '地面有积水，便池未及时清理', SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 4 AND shift_type = 'AFTERNOON');

-- 第-3天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 3, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 3 + 0.34, SYSDATE - 3 + 0.45, 86, '良好', SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 3 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 3, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 3 + 0.35, SYSDATE - 3 + 0.46, 83, '火车站保洁正常', SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 3 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 3, 'EVENING', 'DAILY', 'FINISHED', SYSDATE - 3 + 0.71, SYSDATE - 3 + 0.84, 78, '晚间保洁，整体合格', SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 3 AND shift_type = 'EVENING');

-- 第-2天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 2, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 2 + 0.34, SYSDATE - 2 + 0.44, 80, '合格', SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 2 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 2, 'MORNING', 'DAILY', 'FINISHED', SYSDATE - 2 + 0.35, SYSDATE - 2 + 0.47, 77, '步行街早班保洁，垃圾桶清理及时', SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 2 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 2, 'AFTERNOON', 'DEEP', 'FINISHED', SYSDATE - 2 + 0.55, SYSDATE - 2 + 0.72, 91, '火车站深度保洁，效果明显', SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 2 AND shift_type = 'AFTERNOON');

-- 第-1天
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE - 1, 'MORNING', 'DAILY', 'SIGNED_OFF', SYSDATE - 1 + 0.35, SYSDATE - 1 + 0.46, NULL, NULL, SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE - 1 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 1, 'MORNING', 'DAILY', 'SIGNED_OFF', SYSDATE - 1 + 0.36, SYSDATE - 1 + 0.48, NULL, NULL, SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 1 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE - 1, 'MORNING', 'DAILY', 'SIGNED_OFF', SYSDATE - 1 + 0.35, SYSDATE - 1 + 0.47, NULL, NULL, SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE - 1 AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, checkin_time, signoff_time, score, remark, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE - 1, 'AFTERNOON', 'DAILY', 'SIGNED_OFF', SYSDATE - 1 + 0.55, SYSDATE - 1 + 0.70, NULL, NULL, SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE - 1 AND shift_type = 'AFTERNOON');

-- 第0天（今天）— 待打卡状态
INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE, 'MORNING', 'DAILY', 'PENDING', SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner2'), SYSDATE, 'MORNING', 'DAILY', 'PENDING', SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND schedule_date = SYSDATE AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE, 'AFTERNOON', 'DAILY', 'PENDING', SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE AND shift_type = 'AFTERNOON');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner3'), SYSDATE, 'MORNING', 'DAILY', 'PENDING', SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND schedule_date = SYSDATE AND shift_type = 'MORNING');

INSERT INTO tb_clean_schedule (toilet_id, cleaner_id, schedule_date, shift_type, clean_type, status, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'cleaner1'), SYSDATE, 'MORNING', 'DAILY', 'PENDING', SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_clean_schedule WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND cleaner_id = (SELECT id FROM tb_user WHERE username = 'cleaner1') AND schedule_date = SYSDATE AND shift_type = 'MORNING');

-- ========== 耗材库存数据（每个公厕3种耗材，部分低于下限触发预警） ==========
INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '厕纸', '卷', 50, 20, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '厕纸');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手液', '瓶', 15, 5, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '洗手液');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '垃圾袋', '个', 100, 30, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '垃圾袋');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '厕纸', '卷', 8, 20, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '厕纸');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '洗手液', '瓶', 3, 5, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '洗手液');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '垃圾袋', '个', 45, 30, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '垃圾袋');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '厕纸', '卷', 30, 20, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '厕纸');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '洗手液', '瓶', 2, 5, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '洗手液');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '垃圾袋', '个', 60, 30, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '垃圾袋');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '厕纸', '卷', 5, 15, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND consumable_name = '厕纸');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '洗手液', '瓶', 1, 5, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND consumable_name = '洗手液');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '垃圾袋', '个', 35, 30, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND consumable_name = '垃圾袋');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '厕纸', '卷', 20, 20, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '厕纸');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '洗手液', '瓶', 6, 5, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '洗手液');

INSERT INTO tb_consumable_stock (toilet_id, consumable_name, unit, current_stock, min_stock, update_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '垃圾袋', '个', 80, 30, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_stock WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '垃圾袋');

-- ========== 耗材出入库记录（用于耗材使用量趋势图，约30条记录） ==========
INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '厕纸', 30, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 28
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 28 AND quantity = 30);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手液', 5, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 25
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 25);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '垃圾袋', 40, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 22
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 22);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '厕纸', 25, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 20
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 20);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手液', 3, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 18
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 18);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '厕纸', 35, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 12
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 12);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '垃圾袋', 50, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 8);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手液', 4, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 2);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '厕纸', 40, 'IN', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 3 AND type = 'IN');

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), '洗手液', 10, 'IN', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 1 AND type = 'IN');

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '厕纸', 50, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 26
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 26);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '垃圾袋', 30, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 20
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 20);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '洗手液', 4, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 15
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 15);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '厕纸', 60, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 5);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '洗手液', 10, 'IN', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 1 AND type = 'IN');

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), '厕纸', 100, 'IN', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 2 AND type = 'IN');

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '厕纸', 20, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 10
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 10);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '垃圾袋', 25, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 7
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 7);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), '洗手液', 2, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 4);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '厕纸', 15, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 9);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), '垃圾袋', 20, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 6);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '厕纸', 28, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 11
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '厕纸' AND create_time = SYSDATE - 11);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '洗手液', 3, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '洗手液' AND create_time = SYSDATE - 8);

INSERT INTO tb_consumable_record (toilet_id, consumable_name, quantity, type, operator_id, create_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), '垃圾袋', 35, 'OUT', (SELECT id FROM tb_user WHERE username = 'admin'), SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_consumable_record WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND consumable_name = '垃圾袋' AND create_time = SYSDATE - 3);

-- ========== 公众反馈数据（用于满意度图表） ==========
INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 5, '很干净，洗手台没有积水', SYSDATE - 25
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND submit_time = SYSDATE - 25 AND score = 5);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 4, '环境不错，就是厕纸补充不及时', SYSDATE - 20
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND submit_time = SYSDATE - 20 AND score = 4);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 5, '非常满意，无异味', SYSDATE - 15
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND submit_time = SYSDATE - 15 AND score = 5);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 3, '一般般，洗手液空了', SYSDATE - 10
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND submit_time = SYSDATE - 10 AND score = 3);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '人民广场公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 5, '整体很好，继续保持', SYSDATE - 5
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '人民广场公厕') AND submit_time = SYSDATE - 5 AND score = 5);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 4, '火车站公厕人流量大，能保持这样不错了', SYSDATE - 22
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND submit_time = SYSDATE - 22 AND score = 4);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 2, '地面太脏，希望加强保洁频率', SYSDATE - 18
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND submit_time = SYSDATE - 18 AND score = 2);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 3, '厕所有异味，通风设备需要检查', SYSDATE - 12
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND submit_time = SYSDATE - 12 AND score = 3);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 4, '比之前好多了', SYSDATE - 8
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND submit_time = SYSDATE - 8 AND score = 4);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 2, '洗手台水龙头漏水，没人修', SYSDATE - 3
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '火车站公交车站公厕') AND submit_time = SYSDATE - 3 AND score = 2);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 5, '公园里的公厕，环境优美', SYSDATE - 28
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND submit_time = SYSDATE - 28 AND score = 5);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 4, '干净整洁，建议增加挂钩', SYSDATE - 14
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND submit_time = SYSDATE - 14 AND score = 4);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 3, '排风扇声音太响了', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '朝阳公园北门公厕') AND submit_time = SYSDATE - 6 AND score = 3);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 1, '24小时公厕居然锁门了', SYSDATE - 6
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND submit_time = SYSDATE - 6 AND score = 1);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 2, '滨河公厕卫生太差，无人管理', SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '滨河大道西段公厕') AND submit_time = SYSDATE - 2 AND score = 2);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 5, '步行街公厕位置好找，卫生不错', SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND submit_time = SYSDATE - 9 AND score = 5);

INSERT INTO tb_feedback (toilet_id, user_id, score, content, submit_time)
SELECT (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕'), (SELECT id FROM tb_user WHERE username = 'public1'), 4, '老城区的公厕算干净的了', SYSDATE - 4
WHERE NOT EXISTS (SELECT 1 FROM tb_feedback WHERE toilet_id = (SELECT id FROM tb_toilet WHERE name = '老街步行街中心公厕') AND submit_time = SYSDATE - 4 AND score = 4);

-- ========== 站内消息数据 ==========
INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'cleaner2'), '保洁任务提醒', '您今天（人民广场公厕）有保洁任务，请按时完成。', 1, SYSDATE - 1
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND title = '保洁任务提醒');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'cleaner2'), '保洁评分通知', '您昨天（人民广场公厕）的保洁评分为86分。', 1, SYSDATE - 2
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND title = '保洁评分通知');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'repair1'), '工单派发通知', '有一项新的维修工单已指派给您（人民广场公厕-排风扇异响），请及时处理。', 1, SYSDATE - 9
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'repair1') AND title = '工单派发通知');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'repair1'), '工单完成提醒', '工单 RO20260501001 已完成验收，感谢您的付出。', 1, SYSDATE - 21
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'repair1') AND title = '工单完成提醒');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'repair2'), '工单派发通知', '工单 RO20260510002（火车站-烘手机故障）已指派给您。', 1, SYSDATE - 14
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'repair2') AND title = '工单派发通知');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'cleaner2'), '耗材预警提醒', '您管理的火车站公厕厕纸库存仅剩8卷，低于下限20卷，请及时补充。', 0, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'cleaner2') AND title = '耗材预警提醒');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'cleaner3'), '耗材预警提醒', '您管理的朝阳公园公厕洗手液库存仅剩2瓶，低于下限5瓶。', 0, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'cleaner3') AND title = '耗材预警提醒' AND content LIKE '%朝阳公园%');

INSERT INTO tb_message (user_id, title, content, is_read, create_time)
SELECT (SELECT id FROM tb_user WHERE username = 'admin'), '系统通知', '系统已成功部署达梦数据库，所有功能正常运行。', 0, SYSDATE
WHERE NOT EXISTS (SELECT 1 FROM tb_message WHERE user_id = (SELECT id FROM tb_user WHERE username = 'admin') AND title = '系统通知');

COMMIT;
