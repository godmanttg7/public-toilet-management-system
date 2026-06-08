-- 维修工单照片字段迁移
ALTER TABLE tb_repair_order ADD images VARCHAR(2000) DEFAULT NULL COMMENT '维修照片URL（多个用逗号分隔）';

-- 特殊情况汇报表
CREATE TABLE tb_incident_report (
    id BIGINT NOT NULL IDENTITY(1,1),
    toilet_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reporter_role VARCHAR(20) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    images VARCHAR(2000),
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT SYSDATE,
    update_time TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE tb_incident_report IS '特殊情况汇报表';
COMMENT ON COLUMN tb_incident_report.id IS '汇报编号';
COMMENT ON COLUMN tb_incident_report.toilet_id IS '所属公厕';
COMMENT ON COLUMN tb_incident_report.reporter_id IS '汇报人';
COMMENT ON COLUMN tb_incident_report.reporter_role IS '汇报人角色(CLEANER/REPAIR)';
COMMENT ON COLUMN tb_incident_report.report_type IS '汇报类型(设备异常/安全隐患/物资短缺/其他)';
COMMENT ON COLUMN tb_incident_report.description IS '情况描述';
COMMENT ON COLUMN tb_incident_report.images IS '照片URL（多个用逗号分隔）';
COMMENT ON COLUMN tb_incident_report.status IS '状态(PENDING已上报/RESOLVED已处理)';
COMMENT ON COLUMN tb_incident_report.create_time IS '创建时间';
COMMENT ON COLUMN tb_incident_report.update_time IS '更新时间';
COMMENT ON COLUMN tb_incident_report.deleted IS '逻辑删除（0未删除/1已删除）';
CREATE INDEX idx_incident_toilet ON tb_incident_report(toilet_id);
CREATE INDEX idx_incident_reporter ON tb_incident_report(reporter_id);
