-- =====================================================
-- 智慧养老系统 (Elderly Care) 数据库初始化脚本
-- 根据实体类自动生成
-- 数据库: elderly_care  MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS elderly_care DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elderly_care;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `username`      VARCHAR(255)    DEFAULT NULL              COMMENT '用户名',
    `password`      VARCHAR(255)    DEFAULT NULL              COMMENT '密码(MD5)',
    `real_name`     VARCHAR(100)    DEFAULT NULL              COMMENT '真实姓名',
    `role`          VARCHAR(50)     DEFAULT NULL              COMMENT '角色: admin/staff',
    `status`        VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态: ACTIVE/DISABLED',
    `community`     VARCHAR(255)    DEFAULT NULL              COMMENT '所属社区',
    `account`       VARCHAR(100)    DEFAULT NULL              COMMENT '账号',
    `phone`         VARCHAR(20)     DEFAULT NULL              COMMENT '手机号',
    `email`         VARCHAR(100)    DEFAULT NULL              COMMENT '邮箱',
    `last_login`    DATETIME        DEFAULT NULL              COMMENT '最后登录时间',
    `deleted`       INT             DEFAULT 0                 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_phone` (`phone`),
    INDEX `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 老人表 (elder)
-- =====================================================
DROP TABLE IF EXISTS `elder`;
CREATE TABLE `elder` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(100)    DEFAULT NULL              COMMENT '姓名',
    `gender`        VARCHAR(10)     DEFAULT NULL              COMMENT '性别: MALE/FEMALE',
    `age`           INT             DEFAULT NULL              COMMENT '年龄',
    `id_card`       VARCHAR(18)     DEFAULT NULL              COMMENT '身份证号',
    `birth_date`    DATE            DEFAULT NULL              COMMENT '出生日期',
    `ethnicity`     VARCHAR(50)     DEFAULT NULL              COMMENT '民族',
    `phone`         VARCHAR(20)     DEFAULT NULL              COMMENT '联系电话',
    `address`       VARCHAR(500)    DEFAULT NULL              COMMENT '地址',
    `residence_type` VARCHAR(50)    DEFAULT NULL              COMMENT '居住类型',
    `remark`        VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `user_id`       BIGINT          DEFAULT NULL              COMMENT '关联账号ID(user.id)',
    `device_bound`  TINYINT(1)      DEFAULT 0                 COMMENT '是否绑定设备',
    `deleted`       INT             DEFAULT 0                 COMMENT '逻辑删除',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_elder_name` (`name`),
    INDEX `idx_elder_phone` (`phone`),
    INDEX `idx_elder_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人信息表';

-- =====================================================
-- 3. 告警表 (alert)
-- =====================================================
DROP TABLE IF EXISTS `alert`;
CREATE TABLE `alert` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `alert_type`        VARCHAR(50)     DEFAULT NULL              COMMENT '告警类型: FALL/ABNORMAL_GAIT/DEVICE_OFFLINE/EMERGENCY',
    `level`             VARCHAR(20)     DEFAULT NULL              COMMENT '告警级别: CRITICAL/HIGH/MEDIUM/LOW',
    `description`       VARCHAR(1000)   DEFAULT NULL              COMMENT '告警描述',
    `alert_time`        DATETIME        DEFAULT NULL              COMMENT '告警时间',
    `location`          VARCHAR(255)    DEFAULT NULL              COMMENT '发生位置',
    `status`            VARCHAR(30)     DEFAULT 'PENDING'         COMMENT '处理状态: PENDING/PROCESSING/RESOLVED/IGNORED',
    `handler_id`        BIGINT          DEFAULT NULL              COMMENT '处理人ID',
    `handler`           VARCHAR(100)    DEFAULT NULL              COMMENT '处理人姓名',
    `handle_time`       DATETIME        DEFAULT NULL              COMMENT '处理时间',
    `contact_phone`    VARCHAR(20)      DEFAULT NULL              COMMENT '联系电话',
    `emergency_contact` VARCHAR(100)    DEFAULT NULL              COMMENT '紧急联系人',
    `suggested_action`  VARCHAR(1000)   DEFAULT NULL              COMMENT '建议措施',
    `read`              INT             DEFAULT 0                 COMMENT '是否已读: 0=未读, 1=已读',
    `handle_note`       VARCHAR(1000)   DEFAULT NULL              COMMENT '处理备注',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_alert_elder_id` (`elder_id`),
    INDEX `idx_alert_status` (`status`),
    INDEX `idx_alert_time` (`alert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- =====================================================
-- 4. 设备表 (device)
-- =====================================================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `device_sn`         VARCHAR(100)    DEFAULT NULL              COMMENT '设备序列号',
    `device_name`       VARCHAR(255)    DEFAULT NULL              COMMENT '设备名称',
    `device_type`       VARCHAR(50)     DEFAULT NULL              COMMENT '设备类型: WEARABLE/CAMERA/SENSOR/GATEWAY',
    `status`            VARCHAR(20)     DEFAULT 'ONLINE'          COMMENT '状态: ONLINE/OFFLINE/FAULT/MAINTENANCE',
    `battery`           INT             DEFAULT NULL              COMMENT '电量百分比',
    `firmware`          VARCHAR(100)    DEFAULT NULL              COMMENT '固件版本',
    `bind_elder_name`   VARCHAR(100)    DEFAULT NULL              COMMENT '绑定老人姓名',
    `bind_elder_id`     BIGINT          DEFAULT NULL              COMMENT '绑定老人ID',
    `bind_time`         DATETIME        DEFAULT NULL              COMMENT '绑定时间',
    `last_online_time`  DATETIME        DEFAULT NULL              COMMENT '最后在线时间',
    `location`          VARCHAR(255)    DEFAULT NULL              COMMENT '安装位置',
    `install_date`      DATETIME        DEFAULT NULL              COMMENT '安装日期',
    `alert_rule`        VARCHAR(500)    DEFAULT NULL              COMMENT '告警规则配置',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_device_sn` (`device_sn`),
    INDEX `idx_device_status` (`status`),
    INDEX `idx_device_elder` (`bind_elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备信息表';

-- =====================================================
-- 5. 护工表 (caregiver)
-- =====================================================
DROP TABLE IF EXISTS `caregiver`;
CREATE TABLE `caregiver` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(100)    NOT NULL                  COMMENT '姓名',
    `age`           INT             DEFAULT NULL              COMMENT '年龄',
    `position`      VARCHAR(50)     DEFAULT NULL              COMMENT '职位',
    `phone`         VARCHAR(20)     DEFAULT NULL              COMMENT '手机号',
    `address`       VARCHAR(500)    DEFAULT NULL              COMMENT '地址',
    `user_id`       BIGINT          DEFAULT NULL              COMMENT '关联账号ID(user.id)',
    `community`     VARCHAR(100)    DEFAULT NULL              COMMENT '所属社区',
    `status`        VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态: ACTIVE/INACTIVE',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_caregiver_status` (`status`),
    INDEX `idx_caregiver_user` (`user_id`),
    INDEX `idx_caregiver_community` (`community`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='护工信息表';

-- =====================================================
-- 6. 专家表 (expert)
-- =====================================================
DROP TABLE IF EXISTS `expert`;
CREATE TABLE `expert` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(100)    DEFAULT NULL              COMMENT '姓名',
    `title`         VARCHAR(100)    DEFAULT NULL              COMMENT '职称',
    `specialty`     VARCHAR(200)    DEFAULT NULL              COMMENT '专长领域',
    `institution`   VARCHAR(200)    DEFAULT NULL              COMMENT '所属机构',
    `phone`         VARCHAR(20)     DEFAULT NULL              COMMENT '联系电话',
    `user_id`       BIGINT          DEFAULT NULL              COMMENT '关联账号ID(user.id)',
    `status`        VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态',
    `today_count`   INT             DEFAULT 0                 COMMENT '今日咨询数',
    `total_count`   INT             DEFAULT 0                 COMMENT '累计咨询数',
    `rating`        DECIMAL(3,2)    DEFAULT 0.00              COMMENT '评分(1-5)',
    `schedule`      VARCHAR(500)    DEFAULT NULL              COMMENT '排班信息',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_expert_status` (`status`),
    INDEX `idx_expert_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专家信息表';

-- =====================================================
-- 7. 咨询表 (consult)
-- =====================================================
DROP TABLE IF EXISTS `consult`;
CREATE TABLE `consult` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `elder_id`      BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `type`          VARCHAR(50)     DEFAULT NULL              COMMENT '咨询类型',
    `expert_id`     BIGINT          DEFAULT NULL              COMMENT '专家ID',
    `expert_name`   VARCHAR(100)    DEFAULT NULL              COMMENT '专家姓名',
    `conversation_id` BIGINT        DEFAULT NULL              COMMENT '关联会话ID(conversation.id)',
    `status`        VARCHAR(30)     DEFAULT 'PENDING'         COMMENT '状态: PENDING/IN_PROGRESS/COMPLETED',
    `urgency`       VARCHAR(20)     DEFAULT NULL              COMMENT '紧急程度',
    `consult_time`  DATETIME        DEFAULT NULL              COMMENT '咨询时间',
    `content`       TEXT            DEFAULT NULL              COMMENT '咨询内容',
    `reply`         TEXT            DEFAULT NULL              COMMENT '回复内容',
    `rating`        INT             DEFAULT NULL              COMMENT '评分(1-5)',
    `feedback`      VARCHAR(1000)   DEFAULT NULL              COMMENT '反馈',
    `expert_note`   VARCHAR(1000)   DEFAULT NULL              COMMENT '专家备注(仅专家/管理员可见)',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_consult_expert` (`expert_id`),
    INDEX `idx_consult_status` (`status`),
    INDEX `idx_consult_elder` (`elder_id`),
    INDEX `idx_consult_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询记录表';

-- =====================================================
-- 8. 跌倒记录表 (fall_record)
-- =====================================================
DROP TABLE IF EXISTS `fall_record`;
CREATE TABLE `fall_record` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`      BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `location`      VARCHAR(255)    DEFAULT NULL              COMMENT '跌倒位置',
    `fall_time`     DATETIME        DEFAULT NULL              COMMENT '跌倒时间',
    `cause`         VARCHAR(500)    DEFAULT NULL              COMMENT '原因分析',
    `injury_part`   VARCHAR(200)    DEFAULT NULL              COMMENT '受伤部位',
    `severity`      VARCHAR(20)     DEFAULT NULL              COMMENT '严重程度: MILD/MODERATE/SEVERE',
    `treatment`     VARCHAR(1000)   DEFAULT NULL              COMMENT '处理措施',
    `recover_status` VARCHAR(30)    DEFAULT NULL              COMMENT '恢复状态',
    `handler`       VARCHAR(100)    DEFAULT NULL              COMMENT '处理人',
    `handler_id`    BIGINT          DEFAULT NULL              COMMENT '处理人账号ID(user.id)',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_fall_elder` (`elder_id`),
    INDEX `idx_fall_time` (`fall_time`),
    INDEX `idx_fall_handler` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跌倒记录表';

-- =====================================================
-- 9. 步态记录表 (gait_record)
-- =====================================================
DROP TABLE IF EXISTS `gait_record`;
CREATE TABLE `gait_record` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `elder_id`      BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `date`          DATETIME        DEFAULT NULL              COMMENT '记录日期',
    `speed`         DECIMAL(10,2)   DEFAULT NULL              COMMENT '步速(m/s)',
    `frequency`     INT             DEFAULT NULL              COMMENT '步频(步/分钟)',
    `step_length`   INT             DEFAULT NULL              COMMENT '步长(cm)',
    `score`         INT             DEFAULT NULL              COMMENT '步态评分',
    `result`        VARCHAR(50)     DEFAULT NULL              COMMENT '评估结果: NORMAL/ABNORMAL/BORDERLINE',
    PRIMARY KEY (`id`),
    INDEX `idx_gait_elder` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='步态分析记录表';

-- =====================================================
-- 10. 健康备注表 (health_note)
-- =====================================================
DROP TABLE IF EXISTS `health_note`;
CREATE TABLE `health_note` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`      BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `record_time`   DATETIME        DEFAULT NULL              COMMENT '记录时间',
    `recorder`      VARCHAR(100)    DEFAULT NULL              COMMENT '记录人',
    `recorder_id`   BIGINT          DEFAULT NULL              COMMENT '记录人账号ID(user.id)',
    `note_type`     VARCHAR(50)     DEFAULT NULL              COMMENT '备注类型',
    `content`       TEXT            DEFAULT NULL              COMMENT '备注内容',
    `importance`    VARCHAR(20)     DEFAULT 'NORMAL'          COMMENT '重要程度: NORMAL/IMPORTANT/URGENT',
    `fallow_status` VARCHAR(30)     DEFAULT 'PENDING'         COMMENT '跟进状态: PENDING/FOLLOWING/DONE',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_note_elder` (`elder_id`),
    INDEX `idx_note_recorder` (`recorder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康备注表';

-- =====================================================
-- 11. 干预方案表 (intervention)
-- =====================================================
DROP TABLE IF EXISTS `intervention`;
CREATE TABLE `intervention` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `code`              VARCHAR(50)     DEFAULT NULL              COMMENT '方案编号',
    `name`              VARCHAR(200)    DEFAULT NULL              COMMENT '方案名称',
    `type`              VARCHAR(50)     DEFAULT NULL              COMMENT '干预类型',
    `priority`          VARCHAR(20)     DEFAULT NULL              COMMENT '优先级: URGENT/IMPORTANT/NORMAL',
    `status`            VARCHAR(30)     DEFAULT 'DRAFT'           COMMENT '状态: DRAFT/PENDING/APPROVED/ONGOING/COMPLETED/TERMINATED',
    `leader`            VARCHAR(100)    DEFAULT NULL              COMMENT '负责人',
    `leader_id`         BIGINT          DEFAULT NULL              COMMENT '负责人账号ID(user.id)',
    `target_group`      VARCHAR(200)    DEFAULT NULL              COMMENT '适用对象',
    `total_weeks`       INT             DEFAULT NULL              COMMENT '执行周期(周)',
    `current_week`      INT             DEFAULT 0                 COMMENT '当前周数',
    `frequency`         VARCHAR(50)     DEFAULT NULL              COMMENT '执行频率',
    `start_date`        DATETIME        DEFAULT NULL              COMMENT '开始日期',
    `end_date`          DATETIME        DEFAULT NULL              COMMENT '预计完成日期',
    `associated_elders` JSON            DEFAULT NULL              COMMENT '关联老人ID数组',
    `goals`             JSON            DEFAULT NULL              COMMENT '干预目标数组',
    `measures`          JSON            DEFAULT NULL              COMMENT '具体措施数组',
    `notes`             TEXT            DEFAULT NULL              COMMENT '注意事项',
    `rating`            INT             DEFAULT NULL              COMMENT '总体评分(1-5)',
    `avg_exec_rating`   DOUBLE          DEFAULT NULL              COMMENT '平均执行评分',
    `progress`          INT             DEFAULT 0                 COMMENT '执行进度(%)',
    `executed_count`    INT             DEFAULT 0                 COMMENT '已执行次数',
    `total_count`       INT             DEFAULT 0                 COMMENT '总执行次数',
    `abnormal_count`    INT             DEFAULT 0                 COMMENT '异常次数',
    `records`           JSON            DEFAULT NULL              COMMENT '执行记录数组(JSON)',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_intervention_status` (`status`),
    INDEX `idx_intervention_code` (`code`),
    INDEX `idx_intervention_leader` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='干预方案表';

-- =====================================================
-- 12. 干预执行记录表 (intervention_record)
-- =====================================================
DROP TABLE IF EXISTS `intervention_record`;
CREATE TABLE `intervention_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `intervention_id`   BIGINT          DEFAULT NULL              COMMENT '关联干预方案ID',
    `execute_date`      DATETIME        DEFAULT NULL              COMMENT '执行日期',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `executor`          VARCHAR(100)    DEFAULT NULL              COMMENT '执行人',
    `executor_id`       BIGINT          DEFAULT NULL              COMMENT '执行人账号ID(user.id)',
    `status`            VARCHAR(30)     DEFAULT NULL              COMMENT '执行状态: NORMAL/PARTIAL/SKIPPED/ABNORMAL',
    `rating`            INT             DEFAULT NULL              COMMENT '评分(1-5)',
    `remark`            VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `note`              VARCHAR(1000)   DEFAULT NULL              COMMENT '备注2',
    `attachments`       JSON            DEFAULT NULL              COMMENT '附件数组',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_intervention` (`intervention_id`),
    INDEX `idx_record_executor` (`executor_id`),
    INDEX `idx_record_elder` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='干预执行记录表';

-- =====================================================
-- 13. 社区联动记录表 (community_record)
-- =====================================================
DROP TABLE IF EXISTS `community_record`;
CREATE TABLE `community_record` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `code`          VARCHAR(50)     DEFAULT NULL              COMMENT '记录编号',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `alert_type`    VARCHAR(50)     DEFAULT NULL              COMMENT '告警类型',
    `action`        VARCHAR(200)    DEFAULT NULL              COMMENT '联动动作',
    `handler`       VARCHAR(100)    DEFAULT NULL              COMMENT '处理人',
    `handler_id`    VARCHAR(50)     DEFAULT NULL              COMMENT '处理人ID',
    `result`        VARCHAR(200)    DEFAULT NULL              COMMENT '处理结果',
    `review_status` VARCHAR(30)     DEFAULT NULL              COMMENT '审核状态',
    `time`          DATETIME        DEFAULT NULL              COMMENT '时间',
    `full_time`     DATETIME        DEFAULT NULL              COMMENT '完整时间',
    `description`   TEXT            DEFAULT NULL              COMMENT '描述',
    `attachments`   VARCHAR(1000)   DEFAULT NULL              COMMENT '附件',
    `community`     VARCHAR(200)    DEFAULT NULL              COMMENT '所属社区',
    `alert_code`    VARCHAR(50)     DEFAULT NULL              COMMENT '关联告警编号',
    `review`        VARCHAR(500)    DEFAULT NULL              COMMENT '审核意见',
    `review_time`   DATETIME        DEFAULT NULL              COMMENT '审核时间',
    `reject_reason` VARCHAR(500)    DEFAULT NULL              COMMENT '驳回原因',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_community_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区联动记录表';

-- =====================================================
-- 14. 自定义配置表 (custom_config)
-- =====================================================
DROP TABLE IF EXISTS `custom_config`;
CREATE TABLE `custom_config` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `config_type`   VARCHAR(100)    DEFAULT NULL              COMMENT '配置类型',
    `config_json`   TEXT            DEFAULT NULL              COMMENT '配置JSON内容',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义配置表';

-- =====================================================
-- 15. 数据备份表 (data_backup)
-- =====================================================
DROP TABLE IF EXISTS `data_backup`;
CREATE TABLE `data_backup` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `file_name`     VARCHAR(255)    DEFAULT NULL              COMMENT '文件名',
    `backup_time`   DATETIME        DEFAULT NULL              COMMENT '备份时间',
    `type`          VARCHAR(20)     DEFAULT NULL              COMMENT '备份类型: AUTO/MANUAL',
    `file_size`     VARCHAR(50)     DEFAULT NULL              COMMENT '文件大小',
    `status`        VARCHAR(20)     DEFAULT NULL              COMMENT '状态: SUCCESS/FAILED/IN_PROGRESS',
    `file_path`     VARCHAR(500)    DEFAULT NULL              COMMENT '文件路径',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据备份记录表';

-- =====================================================
-- 16. 菜单表 (menu)
-- =====================================================
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(100)    DEFAULT NULL              COMMENT '菜单名称',
    `path`          VARCHAR(200)    DEFAULT NULL              COMMENT '路由路径',
    `icon`          VARCHAR(100)    DEFAULT NULL              COMMENT '图标',
    `parent_id`     BIGINT          DEFAULT 0                 COMMENT '父菜单ID(0=顶级)',
    `visible`       TINYINT(1)      DEFAULT 1                 COMMENT '是否可见: 0=隐藏, 1=显示',
    `sort`          INT             DEFAULT 0                 COMMENT '排序号',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_menu_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 说明：菜单与角色-菜单映射（menu / role_menu）统一由 seed_data.sql 负责插入，
-- init.sql 不再预置菜单数据（原 13 条旧菜单已废弃，改为按角色分端的完整菜单体系）。

-- =====================================================
-- 17. 模型参数表 (model_param)
-- =====================================================
DROP TABLE IF EXISTS `model_param`;
CREATE TABLE `model_param` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `detection`     JSON            DEFAULT NULL              COMMENT '检测参数 {sensitivity,acceleration,angle,timeWindow,confidence}',
    `evaluation`    JSON            DEFAULT NULL              COMMENT '评估参数 {gaitThreshold,activityThreshold,riskCycle}',
    `alert`         JSON            DEFAULT NULL              COMMENT '告警参数 {delay,cooldown,threshold}',
    `device`        JSON            DEFAULT NULL              COMMENT '设备参数 {sensorFrequency,cameraFps,offlineTimeout}',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型参数表';

-- =====================================================
-- 18. 模型参数版本表 (model_param_version)
-- =====================================================
DROP TABLE IF EXISTS `model_param_version`;
CREATE TABLE `model_param_version` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `version`       VARCHAR(50)     DEFAULT NULL              COMMENT '版本号',
    `detection`     JSON            DEFAULT NULL              COMMENT '检测参数',
    `evaluation`    JSON            DEFAULT NULL              COMMENT '评估参数',
    `alert`         JSON            DEFAULT NULL              COMMENT '告警参数',
    `device`        JSON            DEFAULT NULL              COMMENT '设备参数',
    `operator`      VARCHAR(100)    DEFAULT NULL              COMMENT '操作人',
    `description`   VARCHAR(500)    DEFAULT NULL              COMMENT '版本描述',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型参数版本表';

-- =====================================================
-- 19. 通知表 (notification)
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `user_id`       BIGINT          DEFAULT NULL              COMMENT '用户ID',
    `title`         VARCHAR(200)    DEFAULT NULL              COMMENT '通知标题',
    `content`       TEXT            DEFAULT NULL              COMMENT '通知内容',
    `type`          VARCHAR(50)     DEFAULT NULL              COMMENT '通知类型: ALERT/SYSTEM/REMINDER',
    `link`          VARCHAR(500)    DEFAULT NULL              COMMENT '跳转链接',
    `read`          TINYINT(1)      DEFAULT 0                 COMMENT '是否已读',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_notify_user` (`user_id`),
    INDEX `idx_notify_read` (`read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 20. 操作日志表 (operation_log)
-- =====================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `operate_time`  DATETIME        DEFAULT NULL              COMMENT '操作时间',
    `operator`      VARCHAR(100)    DEFAULT NULL              COMMENT '操作人',
    `operate_type`  VARCHAR(50)     DEFAULT NULL              COMMENT '操作类型: INSERT/UPDATE/DELETE/EXPORT',
    `module`        VARCHAR(100)    DEFAULT NULL              COMMENT '操作模块',
    `content`       TEXT            DEFAULT NULL              COMMENT '操作内容',
    `ip`            VARCHAR(50)     DEFAULT NULL              COMMENT 'IP地址',
    `status`        VARCHAR(20)     DEFAULT NULL              COMMENT '操作状态: SUCCESS/FAILED',
    `error_msg`     VARCHAR(1000)   DEFAULT NULL              COMMENT '错误信息',
    PRIMARY KEY (`id`),
    INDEX `idx_log_time` (`operate_time`),
    INDEX `idx_log_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =====================================================
-- 21. 推送偏好配置表 (preference_config)
-- =====================================================
DROP TABLE IF EXISTS `preference_config`;
CREATE TABLE `preference_config` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `channels`      VARCHAR(500)    DEFAULT NULL              COMMENT '推送渠道配置',
    `level_fiters`  VARCHAR(500)    DEFAULT NULL              COMMENT '级别过滤配置',
    `scenes`        VARCHAR(500)    DEFAULT NULL              COMMENT '场景配置',
    `silent_periods` VARCHAR(500)   DEFAULT NULL              COMMENT '静默时段配置',
    `templates`     VARCHAR(1000)   DEFAULT NULL              COMMENT '模板配置',
    `receivers`     VARCHAR(500)    DEFAULT NULL              COMMENT '接收人配置',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送偏好配置表';

-- =====================================================
-- 22. 推送日志表 (push_log)
-- =====================================================
DROP TABLE IF EXISTS `push_log`;
CREATE TABLE `push_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `family_phone`  VARCHAR(20)     DEFAULT NULL              COMMENT '家属电话',
    `channel`       VARCHAR(50)     DEFAULT NULL              COMMENT '推送渠道: SMS/WECHAT/APP/PUSH',
    `scence`        VARCHAR(100)    DEFAULT NULL              COMMENT '推送场景',
    `content`       VARCHAR(500)    DEFAULT NULL              COMMENT '推送内容(简要)',
    `full_content`  TEXT            DEFAULT NULL              COMMENT '推送内容(完整)',
    `status`        VARCHAR(20)     DEFAULT 'PENDING'         COMMENT '推送状态: PENDING/SUCCESS/FAILED/RETRYING',
    `time`          DATETIME        DEFAULT NULL              COMMENT '推送时间',
    `duration`      BIGINT          DEFAULT NULL              COMMENT '耗时(ms)',
    `retry_count`   INT             DEFAULT 0                 COMMENT '重试次数',
    `response`      VARCHAR(1000)   DEFAULT NULL              COMMENT '响应结果',
    `retry_history` TEXT            DEFAULT NULL              COMMENT '重试历史',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_push_status` (`status`),
    INDEX `idx_push_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送日志表';

-- =====================================================
-- 23. 定时任务表 (schedule_task)
-- =====================================================
DROP TABLE IF EXISTS `schedule_task`;
CREATE TABLE `schedule_task` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `task_code`         VARCHAR(50)     DEFAULT NULL              COMMENT '任务编号',
    `task_type`         VARCHAR(50)     DEFAULT NULL              COMMENT '任务类型',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `location`          VARCHAR(255)    DEFAULT NULL              COMMENT '位置',
    `priority`          VARCHAR(20)     DEFAULT NULL              COMMENT '优先级',
    `task_status`       VARCHAR(30)     DEFAULT NULL              COMMENT '任务状态',
    `trigger_reson`     VARCHAR(500)    DEFAULT NULL              COMMENT '触发原因',
    `relate_alert_code` VARCHAR(50)     DEFAULT NULL              COMMENT '关联告警编号',
    `relate_device_sn`  VARCHAR(100)    DEFAULT NULL              COMMENT '关联设备序列号',
    `handle_result`     VARCHAR(500)    DEFAULT NULL              COMMENT '处理结果',
    `handler`           VARCHAR(100)    DEFAULT NULL              COMMENT '处理人',
    `handler_id`        BIGINT          DEFAULT NULL              COMMENT '处理人账号ID(user.id)',
    `complete_time`     DATETIME        DEFAULT NULL              COMMENT '完成时间',
    `note`              VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `task_date`         DATETIME        DEFAULT NULL              COMMENT '任务日期',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_task_code` (`task_code`),
    INDEX `idx_task_status` (`task_status`),
    INDEX `idx_task_elder` (`elder_id`),
    INDEX `idx_task_handler` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务/调度任务表';

-- =====================================================
-- 24. 空间/房间表 (space)
-- =====================================================
DROP TABLE IF EXISTS `space`;
CREATE TABLE `space` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `code`                  VARCHAR(50)     DEFAULT NULL              COMMENT '空间编号',
    `name`                  VARCHAR(100)    DEFAULT NULL              COMMENT '空间名称',
    `building`              VARCHAR(100)    DEFAULT NULL              COMMENT '楼栋',
    `floor`                 VARCHAR(20)     DEFAULT NULL              COMMENT '楼层',
    `layout`                VARCHAR(50)     DEFAULT NULL              COMMENT '户型',
    `area`                  DOUBLE          DEFAULT NULL              COMMENT '面积(㎡)',
    `occupancy_status`      VARCHAR(30)     DEFAULT 'VACANT'          COMMENT '入住状态: OCCUPIED/VACANT',
    `elder_name`            VARCHAR(100)    DEFAULT NULL              COMMENT '入住老人姓名',
    `elder_id`              BIGINT          DEFAULT NULL              COMMENT '入住老人ID',
    `move_in_date`          DATETIME        DEFAULT NULL              COMMENT '入住时间',
    `last_occupied`         DATETIME        DEFAULT NULL              COMMENT '上次入住时间',
    `facilities`            JSON            DEFAULT NULL              COMMENT '适老化配置数组',
    `risks`                 JSON            DEFAULT NULL              COMMENT '风险因素数组',
    `status`                VARCHAR(30)     DEFAULT 'NORMAL'          COMMENT '空间状态: NORMAL/RENOVATING/REPAIR/INACTIVE',
    `safety_rating`         VARCHAR(10)     DEFAULT NULL              COMMENT '安全评级: A/B/C/D',
    `last_inspection`       DATETIME        DEFAULT NULL              COMMENT '最近检查日期',
    `last_inspection_result` VARCHAR(30)    DEFAULT NULL              COMMENT '最近检查结果: PASS/FAIL/NEED_FIX',
    `next_inspection`       DATETIME        DEFAULT NULL              COMMENT '下次检查日期',
    `remark`                VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `create_time`           DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_space_code` (`code`),
    INDEX `idx_space_status` (`status`),
    INDEX `idx_space_elder` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间/房间信息表';

-- =====================================================
-- 25. 空间安全检查记录表 (space_inspection)
-- =====================================================
DROP TABLE IF EXISTS `space_inspection`;
CREATE TABLE `space_inspection` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `space_id`      BIGINT          DEFAULT NULL              COMMENT '空间ID',
    `date`          DATE            DEFAULT NULL              COMMENT '检查日期',
    `result`        VARCHAR(30)     DEFAULT NULL              COMMENT '检查结果: PASS/FAIL/NEED_FIX',
    `inspector`     VARCHAR(100)    DEFAULT NULL              COMMENT '检查人',
    `type`          VARCHAR(50)     DEFAULT NULL              COMMENT '检查类型: MONTHLY/QUARTERLY/SPECIAL/EMERGENCY',
    `issue`         TEXT            DEFAULT NULL              COMMENT '问题描述',
    `repair_status` VARCHAR(30)     DEFAULT NULL              COMMENT '整改状态',
    `deadline`      DATE            DEFAULT NULL              COMMENT '整改期限',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_inspection_space` (`space_id`),
    INDEX `idx_inspection_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间安全检查记录表';

-- =====================================================
-- 说明：所有演示数据（用户/老人/空间/设备/专家/护工等）统一由 seed_data.sql 负责插入，
-- init.sql 仅负责建表，不再重复插入任何默认数据，避免：
--   1) device_sn 唯一索引冲突（init 与 seed 重复插入同一设备）；
--   2) elder.phone / user.phone 重复导致登录 selectOne 抛 TooManyResultsException。
-- 演示账号密码统一为 123456（MD5: e10adc3949ba59abbe56e057f20f883e），user.status 用小写 active。
-- =====================================================

-- =====================================================
-- 以下为「多端扩展」新增表（26~36），角色取值约定：
--   admin 管理员 / expert 专家 / caregiver 陪护 / supervisor 陪护主管 / elder 老人（家属代办）
-- =====================================================

-- =====================================================
-- 26. 角色-菜单权限映射表 (role_menu)
-- =====================================================
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `role`          VARCHAR(50)     NOT NULL                 COMMENT '角色: admin/expert/caregiver/supervisor/elder',
    `menu_id`       BIGINT          NOT NULL                 COMMENT '菜单ID(关联menu.id)',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_role_menu_role` (`role`),
    INDEX `idx_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单权限映射表';

-- =====================================================
-- 27. 会话表 (conversation) — 专家咨询/联系陪护的聊天会话头
-- =====================================================
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `type`              VARCHAR(20)     DEFAULT NULL              COMMENT '会话类型: CONSULT专家咨询/CAREGIVER联系陪护',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '关联老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `family_user_id`    BIGINT          DEFAULT NULL              COMMENT '家属端账号ID(user.id)',
    `family_name`       VARCHAR(100)    DEFAULT NULL              COMMENT '家属姓名',
    `service_user_id`   BIGINT          DEFAULT NULL              COMMENT '服务方账号ID(user.id, 专家或陪护)',
    `service_id`        BIGINT          DEFAULT NULL              COMMENT '服务方业务ID(expert.id/caregiver.id)',
    `service_name`      VARCHAR(100)    DEFAULT NULL              COMMENT '服务方姓名',
    `status`            VARCHAR(20)     DEFAULT 'OPEN'            COMMENT '状态: OPEN/CLOSED',
    `last_message`      VARCHAR(500)    DEFAULT NULL              COMMENT '最后一条消息摘要',
    `last_message_time` DATETIME        DEFAULT NULL              COMMENT '最后消息时间',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_conv_elder` (`elder_id`),
    INDEX `idx_conv_family` (`family_user_id`),
    INDEX `idx_conv_service` (`service_user_id`),
    INDEX `idx_conv_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- =====================================================
-- 28. 消息表 (message) — 会话内消息流
-- =====================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `conversation_id`   BIGINT          NOT NULL                 COMMENT '会话ID(关联conversation.id)',
    `sender_id`         BIGINT          DEFAULT NULL              COMMENT '发送人账号ID(user.id)',
    `sender_role`       VARCHAR(20)     DEFAULT NULL              COMMENT '发送人角色',
    `sender_name`       VARCHAR(100)    DEFAULT NULL              COMMENT '发送人姓名',
    `content`           TEXT            DEFAULT NULL              COMMENT '文本内容',
    `content_type`      VARCHAR(20)     DEFAULT 'TEXT'            COMMENT '消息类型: TEXT/IMAGE/FILE/SYSTEM',
    `is_read`           INT             DEFAULT 0                 COMMENT '是否已读: 0=未读, 1=已读',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_msg_conv` (`conversation_id`),
    INDEX `idx_msg_sender` (`sender_id`),
    INDEX `idx_msg_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- =====================================================
-- 29. 消息附件表 (message_attachment) — 图片/报告
-- =====================================================
DROP TABLE IF EXISTS `message_attachment`;
CREATE TABLE `message_attachment` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `message_id`        BIGINT          DEFAULT NULL              COMMENT '消息ID(关联message.id)',
    `conversation_id`   BIGINT          DEFAULT NULL              COMMENT '会话ID(冗余, 便于查询)',
    `file_name`         VARCHAR(255)    DEFAULT NULL              COMMENT '原文件名',
    `file_type`         VARCHAR(50)     DEFAULT NULL              COMMENT '文件类型: image/jpeg、application/pdf 等',
    `file_path`         VARCHAR(500)    DEFAULT NULL              COMMENT '存储相对路径',
    `file_size`         BIGINT          DEFAULT NULL              COMMENT '文件大小(字节)',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_att_msg` (`message_id`),
    INDEX `idx_att_conv` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息附件表';

-- =====================================================
-- 30. 老人-陪护关系表 (elder_caregiver)
-- =====================================================
DROP TABLE IF EXISTS `elder_caregiver`;
CREATE TABLE `elder_caregiver` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `caregiver_id`      BIGINT          DEFAULT NULL              COMMENT '陪护ID(caregiver.id)',
    `caregiver_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '陪护姓名',
    `service_type`      VARCHAR(50)     DEFAULT NULL              COMMENT '服务类型: DAILY_CARE/REHAB/MEDICATION 等',
    `start_date`        DATETIME        DEFAULT NULL              COMMENT '服务开始时间',
    `end_date`          DATETIME        DEFAULT NULL              COMMENT '服务结束时间(空=进行中)',
    `status`            VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态: ACTIVE/INACTIVE',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_ec_elder` (`elder_id`),
    INDEX `idx_ec_caregiver` (`caregiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人-陪护关系表';

-- =====================================================
-- 31. 老人-专家关系表 (elder_expert)
-- =====================================================
DROP TABLE IF EXISTS `elder_expert`;
CREATE TABLE `elder_expert` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `expert_id`         BIGINT          DEFAULT NULL              COMMENT '专家ID(expert.id)',
    `expert_name`       VARCHAR(100)    DEFAULT NULL              COMMENT '专家姓名',
    `relation_type`     VARCHAR(50)     DEFAULT NULL              COMMENT '关系类型: CONSULTING咨询/FOLLOW_UP随访',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_ee_elder` (`elder_id`),
    INDEX `idx_ee_expert` (`expert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人-专家关系表';

-- =====================================================
-- 32. 老人-家属关系表 (elder_family)
-- =====================================================
DROP TABLE IF EXISTS `elder_family`;
CREATE TABLE `elder_family` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `family_name`       VARCHAR(100)    DEFAULT NULL              COMMENT '家属姓名',
    `relationship`      VARCHAR(50)     DEFAULT NULL              COMMENT '与老人关系: SON/DAUGHTER/SPOUSE 等',
    `phone`             VARCHAR(20)     DEFAULT NULL              COMMENT '家属电话',
    `family_user_id`    BIGINT          DEFAULT NULL              COMMENT '关联家属端账号ID(user.id, 可空)',
    `is_emergency`      TINYINT(1)      DEFAULT 0                 COMMENT '是否紧急联系人',
    `is_push`           TINYINT(1)      DEFAULT 1                 COMMENT '是否接收推送',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_ef_elder` (`elder_id`),
    INDEX `idx_ef_family_user` (`family_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人-家属关系表';

-- =====================================================
-- 33. 专家排班/请假申请表 (expert_schedule_apply)
-- =====================================================
DROP TABLE IF EXISTS `expert_schedule_apply`;
CREATE TABLE `expert_schedule_apply` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `expert_id`         BIGINT          DEFAULT NULL              COMMENT '专家ID(expert.id)',
    `expert_name`       VARCHAR(100)    DEFAULT NULL              COMMENT '专家姓名',
    `apply_type`        VARCHAR(20)     DEFAULT NULL              COMMENT '申请类型: LEAVE请假/SHIFT换班/SCHEDULE排班变更',
    `start_time`        DATETIME        DEFAULT NULL              COMMENT '开始时间',
    `end_time`          DATETIME        DEFAULT NULL              COMMENT '结束时间',
    `reason`            VARCHAR(500)    DEFAULT NULL              COMMENT '申请原因',
    `status`            VARCHAR(20)     DEFAULT 'PENDING'         COMMENT '状态: PENDING/APPROVED/REJECTED',
    `reviewer_id`       BIGINT          DEFAULT NULL              COMMENT '审批人账号ID(user.id)',
    `reviewer_name`     VARCHAR(100)    DEFAULT NULL              COMMENT '审批人姓名',
    `review_time`       DATETIME        DEFAULT NULL              COMMENT '审批时间',
    `review_comment`    VARCHAR(500)    DEFAULT NULL              COMMENT '审批意见',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_esa_expert` (`expert_id`),
    INDEX `idx_esa_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专家排班/请假申请表';

-- =====================================================
-- 34. 陪护排班表 (caregiver_schedule) — 课程表式
-- =====================================================
DROP TABLE IF EXISTS `caregiver_schedule`;
CREATE TABLE `caregiver_schedule` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `caregiver_id`      BIGINT          DEFAULT NULL              COMMENT '陪护ID(caregiver.id)',
    `caregiver_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '陪护姓名',
    `community`         VARCHAR(100)    DEFAULT NULL              COMMENT '所属社区',
    `work_date`         DATE            DEFAULT NULL              COMMENT '排班日期',
    `shift`             VARCHAR(20)     DEFAULT NULL              COMMENT '班次: MORNING/AFTERNOON/NIGHT/FULL/REST/LEAVE',
    `start_time`        VARCHAR(10)     DEFAULT NULL              COMMENT '上班时间(HH:mm)',
    `end_time`          VARCHAR(10)     DEFAULT NULL              COMMENT '下班时间(HH:mm)',
    `note`              VARCHAR(200)    DEFAULT NULL              COMMENT '备注',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_cs_caregiver` (`caregiver_id`),
    INDEX `idx_cs_community` (`community`),
    INDEX `idx_cs_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪护排班表';

-- =====================================================
-- 34.1 陪护请假/换班申请表 (caregiver_schedule_apply)
-- =====================================================
DROP TABLE IF EXISTS `caregiver_schedule_apply`;
CREATE TABLE `caregiver_schedule_apply` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `caregiver_id`      BIGINT          DEFAULT NULL              COMMENT '陪护ID(caregiver.id)',
    `caregiver_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '陪护姓名',
    `community`         VARCHAR(100)    DEFAULT NULL              COMMENT '所属社区',
    `apply_type`        VARCHAR(20)     DEFAULT NULL              COMMENT '申请类型: LEAVE请假/SHIFT换班',
    `start_time`        DATETIME        DEFAULT NULL              COMMENT '开始时间',
    `end_time`          DATETIME        DEFAULT NULL              COMMENT '结束时间',
    `reason`            VARCHAR(500)    DEFAULT NULL              COMMENT '申请原因',
    `status`            VARCHAR(20)     DEFAULT 'PENDING'         COMMENT '状态: PENDING/APPROVED/REJECTED',
    `reviewer_id`       BIGINT          DEFAULT NULL              COMMENT '审批人账号ID(user.id)',
    `reviewer_name`     VARCHAR(100)    DEFAULT NULL              COMMENT '审批人姓名',
    `review_time`       DATETIME        DEFAULT NULL              COMMENT '审批时间',
    `review_comment`    VARCHAR(500)    DEFAULT NULL              COMMENT '审批意见',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_csa_caregiver` (`caregiver_id`),
    INDEX `idx_csa_community` (`community`),
    INDEX `idx_csa_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪护请假/换班申请表';

-- =====================================================
-- 35. 健康指标表 (health_metric) — 血压/心率/血糖/血氧/体温
-- =====================================================
DROP TABLE IF EXISTS `health_metric`;
CREATE TABLE `health_metric` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `metric_type`       VARCHAR(30)     DEFAULT NULL              COMMENT '指标类型: BLOOD_PRESSURE/HEART_RATE/BLOOD_SUGAR/BLOOD_OXYGEN/TEMPERATURE',
    `systolic`          INT             DEFAULT NULL              COMMENT '收缩压(mmHg, 血压专用)',
    `diastolic`         INT             DEFAULT NULL              COMMENT '舒张压(mmHg, 血压专用)',
    `value`             DECIMAL(10,2)   DEFAULT NULL              COMMENT '指标数值(心率/血糖/血氧/体温)',
    `unit`              VARCHAR(20)     DEFAULT NULL              COMMENT '单位: mmHg/bpm/mmol/L/百分比/摄氏度',
    `source`            VARCHAR(20)     DEFAULT 'SIMULATED'       COMMENT '数据来源: DEVICE/SIMULATED/MANUAL',
    `measure_time`      DATETIME        DEFAULT NULL              COMMENT '测量时间',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_hm_elder` (`elder_id`),
    INDEX `idx_hm_type` (`metric_type`),
    INDEX `idx_hm_time` (`measure_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人健康指标表';

-- =====================================================
-- 36. 一键呼救记录表 (sos_record)
-- =====================================================
DROP TABLE IF EXISTS `sos_record`;
CREATE TABLE `sos_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_id`          BIGINT          DEFAULT NULL              COMMENT '老人ID',
    `elder_name`        VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `family_user_id`    BIGINT          DEFAULT NULL              COMMENT '触发人家属账号ID(user.id)',
    `sos_time`          DATETIME        DEFAULT NULL              COMMENT '呼救时间',
    `location`          VARCHAR(255)    DEFAULT NULL              COMMENT '位置',
    `status`            VARCHAR(20)     DEFAULT 'PENDING'         COMMENT '状态: PENDING/RESPONDING/RESOLVED',
    `handler_id`        BIGINT          DEFAULT NULL              COMMENT '处理人账号ID(user.id)',
    `handler_name`      VARCHAR(100)    DEFAULT NULL              COMMENT '处理人姓名',
    `handle_time`       DATETIME        DEFAULT NULL              COMMENT '处理时间',
    `note`              VARCHAR(500)    DEFAULT NULL              COMMENT '备注',
    `relate_alert_code` VARCHAR(50)     DEFAULT NULL              COMMENT '关联告警编号',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_sos_elder` (`elder_id`),
    INDEX `idx_sos_status` (`status`),
    INDEX `idx_sos_time` (`sos_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一键呼救记录表';

-- =====================================================
-- 初始化完成
-- =====================================================
