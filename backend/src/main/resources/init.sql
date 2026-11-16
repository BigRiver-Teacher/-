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
    `device_bound`  TINYINT(1)      DEFAULT 0                 COMMENT '是否绑定设备',
    `deleted`       INT             DEFAULT 0                 COMMENT '逻辑删除',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_elder_name` (`name`),
    INDEX `idx_elder_phone` (`phone`)
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
    `status`        VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态: ACTIVE/INACTIVE',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_caregiver_status` (`status`)
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
    `status`        VARCHAR(20)     DEFAULT 'ACTIVE'          COMMENT '状态',
    `today_count`   INT             DEFAULT 0                 COMMENT '今日咨询数',
    `total_count`   INT             DEFAULT 0                 COMMENT '累计咨询数',
    `rating`        DECIMAL(3,2)    DEFAULT 0.00              COMMENT '评分(1-5)',
    `schedule`      VARCHAR(500)    DEFAULT NULL              COMMENT '排班信息',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_expert_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专家信息表';

-- =====================================================
-- 7. 咨询表 (consult)
-- =====================================================
DROP TABLE IF EXISTS `consult`;
CREATE TABLE `consult` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `elder_name`    VARCHAR(100)    DEFAULT NULL              COMMENT '老人姓名',
    `type`          VARCHAR(50)     DEFAULT NULL              COMMENT '咨询类型',
    `expert_id`     BIGINT          DEFAULT NULL              COMMENT '专家ID',
    `expert_name`   VARCHAR(100)    DEFAULT NULL              COMMENT '专家姓名',
    `status`        VARCHAR(30)     DEFAULT 'PENDING'         COMMENT '状态: PENDING/IN_PROGRESS/COMPLETED',
    `urgency`       VARCHAR(20)     DEFAULT NULL              COMMENT '紧急程度',
    `consult_time`  DATETIME        DEFAULT NULL              COMMENT '咨询时间',
    `content`       TEXT            DEFAULT NULL              COMMENT '咨询内容',
    `reply`         TEXT            DEFAULT NULL              COMMENT '回复内容',
    `rating`        INT             DEFAULT NULL              COMMENT '评分(1-5)',
    `feedback`      VARCHAR(1000)   DEFAULT NULL              COMMENT '反馈',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_consult_expert` (`expert_id`),
    INDEX `idx_consult_status` (`status`)
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
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_fall_elder` (`elder_id`),
    INDEX `idx_fall_time` (`fall_time`)
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
    `note_type`     VARCHAR(50)     DEFAULT NULL              COMMENT '备注类型',
    `content`       TEXT            DEFAULT NULL              COMMENT '备注内容',
    `importance`    VARCHAR(20)     DEFAULT 'NORMAL'          COMMENT '重要程度: NORMAL/IMPORTANT/URGENT',
    `fallow_status` VARCHAR(30)     DEFAULT 'PENDING'         COMMENT '跟进状态: PENDING/FOLLOWING/DONE',
    `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_note_elder` (`elder_id`)
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
    INDEX `idx_intervention_code` (`code`)
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
    `executor`          VARCHAR(100)    DEFAULT NULL              COMMENT '执行人',
    `status`            VARCHAR(30)     DEFAULT NULL              COMMENT '执行状态: NORMAL/PARTIAL/SKIPPED/ABNORMAL',
    `rating`            INT             DEFAULT NULL              COMMENT '评分(1-5)',
    `remark`            VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `note`              VARCHAR(1000)   DEFAULT NULL              COMMENT '备注2',
    `attachments`       JSON            DEFAULT NULL              COMMENT '附件数组',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_intervention` (`intervention_id`)
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

-- 预置菜单数据
INSERT INTO `menu` (`id`, `name`, `path`, `icon`, `parent_id`, `visible`, `sort`) VALUES
(1, '首页',       '/dashboard',         'dashboard',    0, 1, 1),
(2, '老人管理',   '/elder',             'user',         0, 1, 2),
(3, '设备管理',   '/device',            'device',       0, 1, 3),
(4, '告警中心',   '/alert',             'bell',         0, 1, 4),
(5, '健康监测',   '/health',            'heart',        0, 1, 5),
(6, '空间管理',   '/space',             'building',     0, 1, 6),
(7, '干预方案',   '/intervention',      'file-text',    0, 1, 7),
(8, '咨询管理',   '/consult',           'message',      0, 1, 8),
(9, '护工管理',   '/caregiver',         'user-check',   0, 1, 9),
(10, '专家管理',  '/expert',            'star',         0, 1, 10),
(11, '社区联动',  '/community',         'share',        0, 1, 11),
(12, '数据管理',  '/data',              'database',     0, 1, 12),
(13, '系统设置',  '/settings',          'settings',     0, 1, 13);

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
    `location`          VARCHAR(255)    DEFAULT NULL              COMMENT '位置',
    `priority`          VARCHAR(20)     DEFAULT NULL              COMMENT '优先级',
    `task_status`       VARCHAR(30)     DEFAULT NULL              COMMENT '任务状态',
    `trigger_reson`     VARCHAR(500)    DEFAULT NULL              COMMENT '触发原因',
    `relate_alert_code` VARCHAR(50)     DEFAULT NULL              COMMENT '关联告警编号',
    `relate_device_sn`  VARCHAR(100)    DEFAULT NULL              COMMENT '关联设备序列号',
    `handle_result`     VARCHAR(500)    DEFAULT NULL              COMMENT '处理结果',
    `handler`           VARCHAR(100)    DEFAULT NULL              COMMENT '处理人',
    `complete_time`     DATETIME        DEFAULT NULL              COMMENT '完成时间',
    `note`              VARCHAR(1000)   DEFAULT NULL              COMMENT '备注',
    `task_date`         DATETIME        DEFAULT NULL              COMMENT '任务日期',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        INT             DEFAULT 0                 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_task_code` (`task_code`),
    INDEX `idx_task_status` (`task_status`)
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
-- 插入默认管理员用户 (密码: admin123 的MD5值)
-- =====================================================
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `account`, `phone`, `create_time`)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'admin', 'active', 'admin', '13800000000', NOW());

-- 插入默认老人测试数据
INSERT INTO `elder` (`name`, `gender`, `age`, `phone`, `address`, `residence_type`, `create_time`) VALUES
('张建国', 'MALE', 78, '13900001111', '北京市朝阳区XX小区1号楼101', 'PERMANENT', NOW()),
('李秀英', 'FEMALE', 82, '13900002222', '北京市朝阳区XX小区2号楼203', 'PERMANENT', NOW()),
('王德发', 'MALE', 75, '13900003333', '北京市朝阳区XX小区3号楼305', 'TEMPORARY', NOW());

-- 插入默认空间数据
INSERT INTO `space` (`code`, `name`, `building`, `floor`, `layout`, `area`, `occupancy_status`, `status`, `safety_rating`, `create_time`) VALUES
('A-101', 'A栋101室', 'A栋', '1层', '一室一厅', 45.0, 'VACANT', 'NORMAL', 'A', NOW()),
('A-102', 'A栋102室', 'A栋', '1层', '一室一厅', 45.0, 'VACANT', 'NORMAL', 'A', NOW()),
('B-201', 'B栋201室', 'B栋', '2层', '两室一厅', 62.5, 'VACANT', 'NORMAL', 'B', NOW()),
('B-202', 'B栋202室', 'B栋', '2层', '一室一厅', 48.0, 'VACANT', 'NORMAL', 'B', NOW()),
('C-301', 'C栋301室', 'C栋', '3层', '单人房', 30.0, 'VACANT', 'NORMAL', 'A', NOW());

-- 插入默认设备数据
INSERT INTO `device` (`device_sn`, `device_name`, `device_type`, `status`, `location`, `create_time`) VALUES
('DEV-2024-001', '智能手环-001', 'WEARABLE', 'ONLINE', '仓库', NOW()),
('DEV-2024-002', '跌倒检测摄像头-001', 'CAMERA', 'ONLINE', 'A栋走廊', NOW()),
('DEV-2024-003', '环境传感器-001', 'SENSOR', 'ONLINE', 'B栋大厅', NOW());

-- =====================================================
-- 初始化完成
-- =====================================================
