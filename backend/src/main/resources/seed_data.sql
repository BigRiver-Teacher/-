-- =====================================================
-- 智慧养老系统 (Elderly Care) 测试数据填充脚本
-- 每个表 2-3 条数据，字段与实体类/前端 TS 类型保持一致
-- 使用方式: 先在 init.sql 建库和表，再执行本脚本
--
-- 【重要声明】本文件所有数据均为虚构演示数据：
--   * 姓名、身份证号、电话、地址、机构名均为编造，不指向任何真实个人或机构；
--   * 身份证号后四位已统一置为 0000（校验位无效），仅保留出生日期用于年龄演示；
--   * 医疗机构名称已替换为「示例市XX医院」等通用虚构名。
--   严禁将本文件数据用于任何真实业务或对外发布。
-- =====================================================

USE elderly_care;

-- =====================================================
-- 1. 用户表 (user) — 管理员 + 2 位员工（密码均为 123456 的 MD5）
-- =====================================================
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `community`, `account`, `phone`, `email`, `last_login`, `create_time`)
VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', 'ACTIVE', '朝阳社区', 'admin',    '13800000000', 'admin@example.com',    NOW(), DATE_SUB(NOW(), INTERVAL 365 DAY)),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三',     'staff',  'ACTIVE', '朝阳社区', 'zhangsan', '13800001111', 'zhangsan@example.com', NOW(), DATE_SUB(NOW(), INTERVAL 90 DAY)),
('lisi',     'e10adc3949ba59abbe56e057f20f883e', '李四',     'staff',  'ACTIVE', '海淀社区', 'lisi',     '13800002222', 'lisi@example.com',     NOW(), DATE_SUB(NOW(), INTERVAL 60 DAY));

-- =====================================================
-- 2. 老人表 (elder) — 6 位老人（其它表关联的 elder_id 以此为准）
-- =====================================================
INSERT INTO `elder` (`name`, `gender`, `age`, `id_card`, `birth_date`, `ethnicity`, `phone`, `address`, `residence_type`, `remark`, `device_bound`, `create_time`)
VALUES
('张建国', 'MALE',   78, '110101194505120000', '1945-05-12', '汉族', '13900001111', '北京市朝阳区朝阳路3栋501室', 'PERMANENT', '高血压、冠心病史，需定期测量血压', 1, DATE_SUB(NOW(), INTERVAL 200 DAY)),
('李秀英', 'FEMALE', 82, '110101194208200000', '1942-08-20', '汉族', '13900002222', '北京市朝阳区朝阳路3栋502室', 'PERMANENT', '轻度认知障碍，需要日常陪护', 1, DATE_SUB(NOW(), INTERVAL 180 DAY)),
('王德发', 'MALE',   75, '110101194903080000', '1949-03-08', '汉族', '13900003333', '北京市海淀区学院路8号', 'PERMANENT', '2型糖尿病史，需严格控制饮食', 0, DATE_SUB(NOW(), INTERVAL 160 DAY)),
('赵玉兰', 'FEMALE', 85, '110101193903150000', '1939-03-15', '汉族', '13900004444', '北京市西城区XX胡同12号', 'PERMANENT', '高血压、糖尿病史，行动不便', 1, DATE_SUB(NOW(), INTERVAL 120 DAY)),
('刘德胜', 'MALE',   71, '110101195311020000', '1953-11-02', '汉族', '13900005555', '北京市海淀区XX小区4栋502', 'PERMANENT', '轻度认知障碍，需防走失', 1, DATE_SUB(NOW(), INTERVAL 100 DAY)),
('陈阿妹', 'FEMALE', 92, '110101193207280000', '1932-07-28', '壮族', '13900006666', '北京市丰台区XX街道8号', 'PERMANENT', '行动不便，需轮椅辅助', 0, DATE_SUB(NOW(), INTERVAL 180 DAY));

-- =====================================================
-- 3. 告警表 (alert) — 3 条
-- =====================================================
INSERT INTO `alert` (`elder_name`, `elder_id`, `alert_type`, `level`, `description`, `alert_time`, `location`, `status`, `handler_id`, `handler`, `handle_time`, `contact_phone`, `emergency_contact`, `suggested_action`, `read`, `handle_note`, `create_time`)
VALUES
('张建国', 1, 'FALL', 'high', '老人在A栋走廊跌倒，智能手环检测到异常加速度', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'A栋1楼走廊', 'processing', 2, '张三', NULL, '13900001111', '张小明（儿子）', '立即前往查看，检查是否有骨折，必要时拨打120', 0, '已派护工前往现场', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('李秀英', 2, 'DEVICE_OFFLINE', 'high', '老人佩戴的智能手环超过30分钟未上报数据', DATE_SUB(NOW(), INTERVAL 5 HOUR), 'B栋2楼活动室', 'pending', NULL, NULL, NULL, '13900002222', '李大明（儿子）', '检查手环电量及网络连接，必要时更换设备', 1, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('王德发', 3, 'EMERGENCY', 'medium', '老人主动按下一键呼救按钮', DATE_SUB(NOW(), INTERVAL 1 DAY), 'C栋305室', 'resolved', 2, '张三', DATE_SUB(NOW(), INTERVAL 23 HOUR), '13900003333', '王小华（女儿）', '立即联系老人确认情况，安排护工上门检查', 0, '老人误触，已确认安全无异常', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 4. 设备表 (device) — 6 台
-- =====================================================
INSERT INTO `device` (`device_sn`, `device_name`, `device_type`, `status`, `battery`, `firmware`, `bind_elder_name`, `bind_elder_id`, `bind_time`, `last_online_time`, `location`, `install_date`, `alert_rule`, `create_time`)
VALUES
('DEV-2024-001', '智能手环-001', 'WEARABLE', 'ONLINE', 92, 'v2.1.2', '张建国', 1, DATE_SUB(NOW(), INTERVAL 90 DAY), NOW(), 'A栋101室', DATE_SUB(NOW(), INTERVAL 90 DAY), '{"fall":true,"heartRate":true,"lowBattery":true}', DATE_SUB(NOW(), INTERVAL 90 DAY)),
('DEV-2024-002', '智能手环-002', 'WEARABLE', 'ONLINE', 85, 'v2.1.3', '李秀英', 2, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW(), 'B栋203室', DATE_SUB(NOW(), INTERVAL 60 DAY), '{"fall":true,"heartRate":true,"lowBattery":true}', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('DEV-2024-003', '环境传感器-001', 'SENSOR', 'ONLINE', NULL, 'v1.5.0', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'A栋公共区域', DATE_SUB(NOW(), INTERVAL 45 DAY), NULL, DATE_SUB(NOW(), INTERVAL 45 DAY)),
('DEV-2024-004', '智能摄像头-001', 'CAMERA', 'ONLINE', NULL, 'v3.0.1', '赵玉兰', 4, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), 'A栋302室', DATE_SUB(NOW(), INTERVAL 30 DAY), '{"motionDetection":true,"privacyMask":true}', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('DEV-2024-005', 'GPS定位器-001', 'WEARABLE', 'ONLINE', 63, 'v1.2.0', '刘德胜', 5, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 5 MINUTE), 'B栋401室', DATE_SUB(NOW(), INTERVAL 20 DAY), '{"outOfRange":true,"lowBattery":true}', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('DEV-2024-006', '智能摄像头-002', 'CAMERA', 'OFFLINE', NULL, 'v3.0.1', '李秀英', 2, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'B栋203室', DATE_SUB(NOW(), INTERVAL 30 DAY), '{"motionDetection":true}', DATE_SUB(NOW(), INTERVAL 30 DAY));

-- =====================================================
-- 5. 护工表 (caregiver) — 3 名
-- =====================================================
INSERT INTO `caregiver` (`name`, `age`, `position`, `phone`, `address`, `status`, `create_time`)
VALUES
('周丽华', 38, 'SENIOR_CAREGIVER', '13800007777', '北京市朝阳区XX街道15号', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 200 DAY)),
('吴晓芳', 28, 'CAREGIVER', '13800008888', '北京市海淀区XX路22号', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 150 DAY)),
('郑国栋', 45, 'NURSING_SUPERVISOR', '13800009999', '北京市丰台区XX小区7栋301', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 365 DAY));

-- =====================================================
-- 6. 专家表 (expert) — 3 位
-- =====================================================
INSERT INTO `expert` (`name`, `title`, `specialty`, `institution`, `phone`, `status`, `today_count`, `total_count`, `rating`, `schedule`, `create_time`)
VALUES
('陈伟明', '主任医师', '老年心血管疾病', '示例市第一人民医院', '13800010001', 'ACTIVE', 3, 156, 4.85, '{"mon":"AM","wed":"PM","fri":"AM"}', DATE_SUB(NOW(), INTERVAL 400 DAY)),
('林美丽', '副主任医师', '老年神经系统疾病', '示例市第二人民医院', '13800010002', 'ACTIVE', 1, 98, 4.62, '{"tue":"FULL","thu":"FULL"}', DATE_SUB(NOW(), INTERVAL 300 DAY)),
('黄志强', '教授', '老年骨科与康复医学', '示例市康复中心医院', '13800010003', 'ACTIVE', 0, 72, 4.91, '{"mon":"PM","wed":"AM","sat":"AM"}', DATE_SUB(NOW(), INTERVAL 250 DAY));

-- =====================================================
-- 7. 咨询表 (consult) — 3 条
-- =====================================================
INSERT INTO `consult` (`elder_name`, `type`, `expert_id`, `expert_name`, `status`, `urgency`, `consult_time`, `content`, `reply`, `rating`, `feedback`, `create_time`)
VALUES
('张建国', '健康咨询', 1, '陈伟明', 'completed', 'urgent', DATE_SUB(NOW(), INTERVAL 3 DAY), '老人近期频繁出现心悸、胸闷症状，血压波动较大（150-170/90-100），需要专家评估是否需要调整用药方案。', '根据描述，建议先做24小时动态心电图和血压监测。目前用药方案可暂时维持，待检查结果出来后再调整。建议低盐低脂饮食，每日监测血压并记录。', 5, '专家回复非常及时和专业，家属很满意', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('李秀英', '健康咨询', 2, '林美丽', 'ongoing', 'important', DATE_SUB(NOW(), INTERVAL 1 DAY), '老人近期记忆力明显下降，出现走失风险，家属咨询是否为阿尔茨海默病早期症状。', '建议尽快进行MMSE量表评估和头颅MRI检查。家属可先准备定位设备、避免老人单独外出、保持规律作息。', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('赵玉兰', '康复建议', 3, '黄志强', 'pending', 'normal', NOW(), '老人自诉腰部酸疼，询问是否适合进行康复运动训练。', NULL, NULL, NULL, NOW());

-- =====================================================
-- 8. 跌倒记录表 (fall_record) — 3 条
-- =====================================================
INSERT INTO `fall_record` (`elder_id`, `elder_name`, `location`, `fall_time`, `cause`, `injury_part`, `severity`, `treatment`, `recover_status`, `handler`, `create_time`)
VALUES
(1, '张建国', 'A栋101室卫生间', DATE_SUB(NOW(), INTERVAL 7 DAY), '地面湿滑导致失去平衡', '右髋部', 'MODERATE', '立即冰敷，呼叫急救送往医院拍片检查，排除骨折后卧床休息', 'RECOVERING', '张三', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, '王德发', 'C栋305室客厅', DATE_SUB(NOW(), INTERVAL 14 DAY), '夜间起床未开灯，被地毯绊倒', '左前臂', 'MILD', '局部涂药包扎，密切观察', 'RECOVERED', '周丽华', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(2, '李秀英', 'B栋大堂入口', DATE_SUB(NOW(), INTERVAL 1 DAY), '下雨天路面湿滑，拐杖支撑不稳', '左膝', 'MILD', '局部冷敷，涂活血化瘀药膏', 'RECOVERING', '吴晓芳', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 9. 步态记录表 (gait_record) — 3 条
-- =====================================================
INSERT INTO `gait_record` (`name`, `elder_id`, `date`, `speed`, `frequency`, `step_length`, `score`, `result`)
VALUES
('张建国', 1, NOW(), 0.82, 95, 52, 72, 'normal'),
('李秀英', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 0.58, 78, 38, 45, 'BORDERLINE'),
('王德发', 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 1.05, 108, 60, 88, 'normal');

-- =====================================================
-- 10. 健康备注表 (health_note) — 3 条
-- =====================================================
INSERT INTO `health_note` (`elder_id`, `elder_name`, `record_time`, `recorder`, `note_type`, `content`, `importance`, `fallow_status`, `create_time`)
VALUES
(1, '张建国', NOW(), '周丽华', 'BLOOD_PRESSURE', '今日晨起血压160/95，偏高。已提醒老人按时服用降压药，建议低盐饮食。午后复测血压145/88，略有下降。', 'IMPORTANT', 'FOLLOWING', NOW()),
(2, '李秀英', DATE_SUB(NOW(), INTERVAL 2 HOUR), '吴晓芳', 'DAILY_CHECK', '日常巡视时发现老人情绪低落，询问后得知昨晚睡眠质量差。已陪老人聊天安抚情绪，安排午睡。注意后续观察。', 'normal', 'pending', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, '王德发', DATE_SUB(NOW(), INTERVAL 1 DAY), '郑国栋', 'MEDICATION', '老人反映近来胃部不适，可能与新换的降压药有关。已联系社区医生，建议暂停新药恢复原用药方案，观察三天。', 'URGENT', 'FOLLOWING', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 11. 干预方案表 (intervention) — 3 条
-- =====================================================
INSERT INTO `intervention` (`code`, `name`, `type`, `priority`, `status`, `leader`, `target_group`, `total_weeks`, `current_week`, `frequency`, `start_date`, `end_date`, `associated_elders`, `goals`, `measures`, `notes`, `rating`, `avg_exec_rating`, `progress`, `executed_count`, `total_count`, `abnormal_count`, `create_time`)
VALUES
('INT-2024-001', '张建国跌倒预防方案', 'FALL_PREVENTION', 'URGENT', 'ONGOING', '周丽华', '高龄+跌倒史', 8, 3, 'DAILY', DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 21 DAY), INTERVAL 56 DAY),
 '["1"]', '["降低跌倒风险至50%以下","提升下肢肌力","改善居家环境安全"]', '["每日下肢力量训练30分钟","安装卫生间扶手和防滑地垫","每周平衡能力评估","定期检查辅助器具"]',
 '老人配合度较好，训练需循序渐进，切勿操之过急', 4, 4.2, 38, 18, 48, 2, DATE_SUB(NOW(), INTERVAL 21 DAY)),
('INT-2024-002', '李秀英认知干预方案', 'COGNITIVE_TRAINING', 'IMPORTANT', 'ONGOING', '吴晓芳', '轻度认知障碍', 12, 5, 'THREE_TIMES_WEEKLY', DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 35 DAY), INTERVAL 84 DAY),
 '["2"]', '["延缓认知功能下降","提高日常生活自理能力","降低走失风险"]', '["每周三次认知训练（记忆、计算、语言）","佩戴GPS定位手环","建立每日活动规律表","家属每周至少探访2次"]',
 '需持续关注老人情绪变化，训练时保持耐心鼓励', 4, 3.8, 42, 15, 36, 1, DATE_SUB(NOW(), INTERVAL 35 DAY)),
('INT-2024-003', '王德发高血压管理方案', 'CHRONIC_DISEASE', 'normal', 'ONGOING', '郑国栋', '高血压患者', 4, 2, 'DAILY', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 28 DAY),
 '["3"]', '["将血压控制在140/90以下","建立规律服药习惯","改善饮食结构"]', '["每日早晚测量血压并记录","按时按量服药","低盐低脂饮食（每日盐<5g）","每周至少3次有氧散步30分钟"]',
 '老人饮食偏好偏咸，需要持续提醒和引导', NULL, NULL, 50, 14, 28, 0, DATE_SUB(NOW(), INTERVAL 14 DAY));

-- =====================================================
-- 12. 干预执行记录表 (intervention_record) — 3 条
-- =====================================================
INSERT INTO `intervention_record` (`intervention_id`, `execute_date`, `elder_name`, `executor`, `status`, `rating`, `remark`, `note`, `create_time`)
VALUES
(1, DATE_SUB(NOW(), INTERVAL 1 DAY), '张建国', '周丽华', 'normal', 5, '今日完成全部训练项目，老人状态良好，下肢力量测试较上周提升', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, DATE_SUB(NOW(), INTERVAL 2 DAY), '张建国', '周丽华', 'PARTIAL', 3, '老人因腰部不适，仅完成50%训练量', '已调整训练方案为轻度拉伸，明日观察恢复情况复查', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY), '李秀英', '吴晓芳', 'normal', 4, '记忆力训练完成较好，语言流畅度较稳定', '老人今日配合度较昨日高，对图片记忆游戏兴趣浓厚', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 13. 社区联动记录表 (community_record) — 3 条
-- =====================================================
INSERT INTO `community_record` (`code`, `elder_name`, `alert_type`, `action`, `handler`, `handler_id`, `result`, `review_status`, `time`, `full_time`, `description`, `community`, `alert_code`, `review`, `review_time`, `create_time`)
VALUES
('CL-2024001', '张建国', 'FALL', '社区工作人员上门探访', '社区网格员-王丽', 'GRID-001', '已上门探访，确认老人已得到妥善处理', 'APPROVED', NOW(), NOW(), '老人于上午跌倒报警后，社区联动机制启动，网格员在30分钟内上门确认情况，老人意识清醒无外伤。', '朝阳社区', 'ALT-2024001', '处理及时，程序合规', DATE_ADD(NOW(), INTERVAL 10 MINUTE), NOW()),
('CL-2024002', '李秀英', 'DEVICE_OFFLINE', '电话联系家属确认', '社区工作人员-赵强', 'COMM-002', '已联系家属，家属已前往老人住所查看', 'PENDING_REVIEW', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), '老人手环离线后，社区联动平台自动通知家属和社区工作人员。家属反馈老人手环忘记充电已处理。', '海淀社区', 'ALT-2024002', NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('CL-2024003', '赵玉兰', 'EMERGENCY', '联合卫生院上门', '社区主任-马建军', 'DIR-001', '已协调卫生院医生上门为老人检查', 'REJECTED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '老人自述头晕恶心，社区联动协调卫生院医生上门检查。但因医生上门时间超出预期，流程待优化。', '西城社区', 'ALT-2024003', '处理流程不规范，需重新提交完整报告', DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 14. 自定义配置表 (custom_config) — sidebar/dashboard/mobile 三种配置
-- 结构与后端 DTO（SidebarConfigDTO/DashboardConfigDTO/MobileConfigDTO）保持一致
-- =====================================================
INSERT INTO `custom_config` (`config_type`, `config_json`, `create_time`)
VALUES
('sidebar', '{"groups":[{"name":"工作台","id":"home"},{"name":"数据管理","id":"data"},{"name":"告警联动","id":"alert"},{"name":"系统设置","id":"system"}],"items":[{"id":"home-management","name":"首页概览","icon":"HomeFilled","group":"home","visible":true},{"id":"data-elder","name":"老人档案","icon":"User","group":"data","visible":true},{"id":"data-device","name":"设备管理","icon":"Monitor","group":"data","visible":true},{"id":"alert-list","name":"告警列表","icon":"Warning","group":"alert","visible":true},{"id":"system-log","name":"操作日志","icon":"Document","group":"system","visible":true}]}', NOW()),
('dashboard', '{"cards":[{"name":"总方案数","id":"1","icon":"Document","type":"统计","visible":true,"colspan":1,"color":"#409eff","previewValue":"45"},{"name":"进行中","id":"2","icon":"Timer","type":"统计","visible":true,"colspan":1,"color":"#67c23a","previewValue":"12"},{"name":"今日预警","id":"7","icon":"Warning","type":"统计","visible":false,"colspan":2,"color":"#f56c6c","previewValue":"3"},{"name":"在线设备","id":"8","icon":"Grid","type":"统计","visible":false,"colspan":1,"color":"#67c23a","previewValue":"32"}]}', NOW()),
('mobile', '{"tabList":[{"name":"首页","id":"home","icon":"HomeFilled","path":"/home","visible":true},{"name":"预警","id":"warning","icon":"Warning","path":"/warning","visible":true},{"name":"老人","id":"elder","icon":"User","path":"/elder","visible":true},{"name":"设备","id":"device","icon":"Monitor","path":"/device","visible":true},{"name":"我的","id":"profile","icon":"UserFilled","path":"/profile","visible":true}],"menuList":[{"name":"健康档案","id":"health","icon":"Document","visible":true},{"name":"步态分析","id":"gait","icon":"TrendCharts","visible":true},{"name":"跌倒记录","id":"fall","icon":"WarningFilled","visible":true}],"featureList":[{"name":"一键呼救","id":"sos","icon":"PhoneFilled","visible":true,"badge":""},{"name":"健康周报","id":"report","icon":"DataAnalysis","visible":true,"badge":"NEW"}],"themeColors":{"primary":"#409eff","secondary":"#67c23a","background":"#f5f7fa"},"pushSetting":{"enabled":true,"sound":true,"banner":true,"content":"您有一条新的健康提醒"}}', NOW());

-- =====================================================
-- 15. 数据备份表 (data_backup) — 3 条
-- =====================================================
INSERT INTO `data_backup` (`file_name`, `backup_time`, `type`, `file_size`, `status`, `file_path`)
VALUES
('elderly_care_20240809_020000.sql', DATE_SUB(NOW(), INTERVAL 1 DAY), 'AUTO', '128.5 MB', 'SUCCESS', '/backup/auto/elderly_care_20240809_020000.sql'),
('elderly_care_20240808_020000.sql', DATE_SUB(NOW(), INTERVAL 2 DAY), 'AUTO', '127.8 MB', 'SUCCESS', '/backup/auto/elderly_care_20240808_020000.sql'),
('elderly_care_manual_20240807.sql', DATE_SUB(NOW(), INTERVAL 3 DAY), 'MANUAL', '129.1 MB', 'SUCCESS', '/backup/manual/elderly_care_manual_20240807.sql');

-- =====================================================
-- 16. 菜单表 (menu) — init.sql 已预置，此处补充动态菜单
-- =====================================================
INSERT INTO `menu` (`name`, `path`, `icon`, `parent_id`, `visible`, `sort`, `create_time`)
VALUES
('首页概览', '/home/management', 'HomeFilled', 0, 1, 1, NOW()),
('预警管理', '/home/warning', 'Warning', 0, 1, 2, NOW()),
('老人档案', '/home/elder/base', 'User', 0, 1, 3, NOW()),
('设备管理', '/home/device', 'Monitor', 0, 1, 4, NOW()),
('专家管理', '/home/expert', 'Star', 0, 1, 5, NOW());

-- =====================================================
-- 17. 模型参数表 (model_param) — 2 条（数值与 DTO 类型一致）
-- =====================================================
INSERT INTO `model_param` (`detection`, `evaluation`, `alert`, `device`, `create_time`)
VALUES
('{"sensitivity":85,"acceleration":0.92,"angle":45.0,"timeWindow":3000,"confidence":80}',
 '{"gaitThreshold":75,"activityThreshold":65,"riskCycle":7}',
 '{"delay":60,"cooldown":300,"threshold":80}',
 '{"sensorFrequency":100,"cameraFps":25,"offlineTimeout":1800}',
 NOW()),
('{"sensitivity":78,"acceleration":0.88,"angle":50.0,"timeWindow":2500,"confidence":75}',
 '{"gaitThreshold":70,"activityThreshold":60,"riskCycle":14}',
 '{"delay":120,"cooldown":600,"threshold":85}',
 '{"sensorFrequency":50,"cameraFps":15,"offlineTimeout":3600}',
 DATE_SUB(NOW(), INTERVAL 30 DAY));

-- =====================================================
-- 18. 模型参数版本表 (model_param_version) — 3 条
-- =====================================================
INSERT INTO `model_param_version` (`version`, `detection`, `evaluation`, `alert`, `device`, `operator`, `description`, `create_time`)
VALUES
('v1.0.0', '{"sensitivity":70,"acceleration":0.80,"angle":40.0,"timeWindow":3000,"confidence":70}', '{"gaitThreshold":65,"activityThreshold":55,"riskCycle":7}', '{"delay":60,"cooldown":300,"threshold":80}', '{"sensorFrequency":50,"cameraFps":15,"offlineTimeout":3600}', '系统管理员', '初始版本，基线参数配置', DATE_SUB(NOW(), INTERVAL 90 DAY)),
('v1.1.0', '{"sensitivity":78,"acceleration":0.88,"angle":50.0,"timeWindow":2500,"confidence":75}', '{"gaitThreshold":70,"activityThreshold":60,"riskCycle":14}', '{"delay":120,"cooldown":600,"threshold":85}', '{"sensorFrequency":50,"cameraFps":15,"offlineTimeout":3600}', '系统管理员', '优化灵敏度参数，降低误报率', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('v1.2.0', '{"sensitivity":85,"acceleration":0.92,"angle":45.0,"timeWindow":3000,"confidence":80}', '{"gaitThreshold":75,"activityThreshold":65,"riskCycle":7}', '{"delay":60,"cooldown":300,"threshold":80}', '{"sensorFrequency":100,"cameraFps":25,"offlineTimeout":1800}', '系统管理员', '引入置信度评分机制，提升检测准确率', DATE_SUB(NOW(), INTERVAL 30 DAY));

-- =====================================================
-- 19. 通知表 (notification) — 3 条
-- =====================================================
INSERT INTO `notification` (`user_id`, `title`, `content`, `type`, `link`, `read`, `create_time`)
VALUES
(1, '新告警通知', '张建国老人于A栋走廊发生跌倒告警，请及时处理！', 'ALERT', '/alert/detail/1', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, '设备离线提醒', '李秀英老人的智能手环已离线超过30分钟，请检查。', 'SYSTEM', '/device/detail/2', 1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, '周报提醒', '请于本周五前完成老人健康周报的提交。', 'REMINDER', '/health/weekly-report', 0, NOW());

-- =====================================================
-- 20. 操作日志表 (operation_log) — 3 条
-- =====================================================
INSERT INTO `operation_log` (`operate_time`, `operator`, `operate_type`, `module`, `content`, `ip`, `status`)
VALUES
(NOW(), '张三', 'UPDATE', '老人管理', '更新老人赵玉兰的备注信息：补充高血压病史', '192.168.1.100', 'SUCCESS'),
(DATE_SUB(NOW(), INTERVAL 30 MINUTE), '系统管理员', 'INSERT', '设备管理', '新增智能手环设备: DEV-2024-004', '192.168.1.1', 'SUCCESS'),
(DATE_SUB(NOW(), INTERVAL 2 HOUR), '李四', 'DELETE', '告警管理', '删除过期告警记录ID: 999（已处理超30天）', '192.168.1.101', 'SUCCESS');

-- =====================================================
-- 21. 推送偏好配置表 (preference_config) — 结构与 PreferenceConfigDTO 一致
-- =====================================================
INSERT INTO `preference_config` (`channels`, `level_fiters`, `scenes`, `silent_periods`, `templates`, `receivers`, `create_time`)
VALUES
('[{"code":"SMS","name":"短信","enable":true},{"code":"APP","name":"APP推送","enable":true},{"code":"WECHAT","name":"微信","enable":true}]',
 '[{"code":"CRITICAL","name":"紧急","channels":["SMS","APP"]},{"code":"HIGH","name":"高级","channels":["SMS","APP","WECHAT"]}]',
 '[{"name":"跌倒告警","enable":true,"channels":["SMS","APP"],"delayMin":0,"silentMode":false},{"name":"设备离线","enable":true,"channels":["APP"],"delayMin":30,"silentMode":true}]',
 '[{"start":"22:00","end":"07:00","name":"夜间静默"}]',
 '[{"scene":"跌倒告警","content":"{elderName}于{location}发生跌倒，请立即查看！"},{"scene":"设备离线","content":"{deviceName}已离线{minutes}分钟，请确认老人状态"}]',
 '[{"enderName":"张建国","managerName":"张三","managerPhone":"13800001111","familyPhone":"13900001111","familyName":"张小明","backupName":"李四","backupPhone":"13800002222"}]',
 NOW());

-- =====================================================
-- 22. 推送日志表 (push_log) — 3 条
-- =====================================================
INSERT INTO `push_log` (`elder_name`, `family_phone`, `channel`, `scence`, `content`, `full_content`, `status`, `time`, `duration`, `retry_count`, `response`, `create_time`)
VALUES
('张建国', '13900001111', 'SMS', 'FALL_ALERT', '【紧急】张建国于A栋走廊发生跌倒，请立即联系养老中心！', '老人张建国于今日在A栋走廊发生疑似跌倒事件，智能手环检测到异常加速度变化。已安排工作人员前往查看。请家属保持电话畅通。', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 HOUR), 1520, 0, 'SMS发送成功，已送达', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('李秀英', '13900002222', 'APP', 'DEVICE_OFFLINE', '李秀英的智能手环已离线超过30分钟，请及时确认老人状态。', NULL, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 HOUR), 320, 0, 'APP推送成功', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('王德发', '13900003333', 'WECHAT', 'DAILY_HEALTH', '王德发今日健康数据已更新：血压138/85，心率72，步数3820。', NULL, 'FAILED', DATE_SUB(NOW(), INTERVAL 1 DAY), 0, 2, '微信模板消息发送失败: token过期', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 23. 定时任务表 (schedule_task) — 3 条
-- =====================================================
INSERT INTO `schedule_task` (`task_code`, `task_type`, `elder_name`, `location`, `priority`, `task_status`, `trigger_reson`, `relate_alert_code`, `relate_device_sn`, `handle_result`, `handler`, `complete_time`, `note`, `task_date`, `create_time`)
VALUES
('TASK-2024001', 'HEALTH_CHECK', '张建国', 'A栋101室', 'urgent', 'processing', '跌倒后第3天健康复检', 'ALT-2024001', 'DEV-2024-001', NULL, '周丽华', NULL, '携带血压计和血糖仪，记录老人恢复情况', CURDATE(), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TASK-2024002', 'DEVICE_REPAIR', '李秀英', 'B栋203室', 'important', 'pending', '智能手环持续离线，需人工检查', 'ALT-2024002', 'DEV-2024-002', NULL, '吴晓芳', NULL, '检查手环是否需充电、同步或更换', CURDATE(), DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('TASK-2024003', 'ENV_INSPECT', NULL, 'C栋公共区域', 'normal', 'completed', '月度例行安全检查', NULL, NULL, '全部区域检查完毕，灭火器压力正常，应急灯功能正常。发现C栋卫生间地垫磨损需更换。', '郑国栋', DATE_SUB(NOW(), INTERVAL 1 HOUR), '地垫已下单采购，预计2天到货', CURDATE(), DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- =====================================================
-- 24. 空间表 (space) — 8 间房
-- =====================================================
INSERT INTO `space` (`code`, `name`, `building`, `floor`, `layout`, `area`, `occupancy_status`, `elder_name`, `elder_id`, `move_in_date`, `facilities`, `risks`, `status`, `safety_rating`, `last_inspection`, `last_inspection_result`, `next_inspection`, `remark`, `create_time`)
VALUES
('A-101', 'A栋101室', 'A栋', '1层', '一室一厅', 45.0, 'occupied', '张建国', 1, DATE_SUB(NOW(), INTERVAL 90 DAY), '["扶手","防滑地垫","紧急呼叫铃","智能感应灯"]', '["卫生间地面湿滑","夜间起床跌倒风险"]', 'normal', 'B', DATE_SUB(NOW(), INTERVAL 30 DAY), 'PASS', DATE_ADD(CURDATE(), INTERVAL 30 DAY), '卫生间地垫已更换为防滑型', DATE_SUB(NOW(), INTERVAL 120 DAY)),
('B-203', 'B栋203室', 'B栋', '2层', '两室一厅', 62.0, 'occupied', '李秀英', 2, DATE_SUB(NOW(), INTERVAL 60 DAY), '["扶手","淋浴椅","GPS定位手环"]', '["认知障碍走失风险","忘记关煤气风险"]', 'normal', 'B', DATE_SUB(NOW(), INTERVAL 15 DAY), 'NEED_FIX', DATE_ADD(CURDATE(), INTERVAL 15 DAY), '厨房已安装煤气泄漏报警器，待复查', DATE_SUB(NOW(), INTERVAL 80 DAY)),
('C-302', 'C栋302室', 'C栋', '3层', '单人房', 30.0, 'occupied', '王德发', 3, DATE_SUB(NOW(), INTERVAL 45 DAY), '["紧急呼叫铃"]', '["高血压突发风险"]', 'repair', 'C', NULL, NULL, NULL, '空调故障，正在安排维修', DATE_SUB(NOW(), INTERVAL 50 DAY)),
('A-302', 'A栋302室', 'A栋', '3层', '两室一厅', 58.0, 'occupied', '赵玉兰', 4, DATE_SUB(NOW(), INTERVAL 30 DAY), '["扶手","防滑地垫"]', '["下肢无力跌倒风险"]', 'normal', 'A', DATE_SUB(NOW(), INTERVAL 10 DAY), 'PASS', DATE_ADD(CURDATE(), INTERVAL 20 DAY), NULL, DATE_SUB(NOW(), INTERVAL 30 DAY)),
('B-401', 'B栋401室', 'B栋', '4层', '一室一厅', 42.0, 'occupied', '刘德胜', 5, DATE_SUB(NOW(), INTERVAL 20 DAY), '["紧急呼叫铃","GPS定位手环"]', '["认知障碍走失风险"]', 'normal', 'B', DATE_SUB(NOW(), INTERVAL 5 DAY), 'PASS', DATE_ADD(CURDATE(), INTERVAL 25 DAY), NULL, DATE_SUB(NOW(), INTERVAL 20 DAY)),
('C-502', 'C栋502室', 'C栋', '5层', '单人房', 28.0, 'occupied', '陈阿妹', 6, DATE_SUB(NOW(), INTERVAL 15 DAY), '["扶手"]', '["行动不便"]', 'normal', 'C', NULL, NULL, NULL, '需轮椅通道改造', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('A-202', 'A栋202室', 'A栋', '2层', '一室一厅', 40.0, 'vacant', NULL, NULL, NULL, '["扶手"]', '[]', 'normal', 'A', DATE_SUB(NOW(), INTERVAL 7 DAY), 'PASS', DATE_ADD(CURDATE(), INTERVAL 23 DAY), NULL, DATE_SUB(NOW(), INTERVAL 200 DAY)),
('B-102', 'B栋102室', 'B栋', '1层', '一室一厅', 42.0, 'vacant', NULL, NULL, NULL, '["扶手","防滑地垫"]', '[]', 'normal', 'A', DATE_SUB(NOW(), INTERVAL 3 DAY), 'PASS', DATE_ADD(CURDATE(), INTERVAL 27 DAY), NULL, DATE_SUB(NOW(), INTERVAL 180 DAY));

-- =====================================================
-- 25. 空间安全检查记录表 (space_inspection) — 3 条
-- =====================================================
INSERT INTO `space_inspection` (`space_id`, `date`, `result`, `inspector`, `type`, `issue`, `repair_status`, `deadline`, `create_time`)
VALUES
(1, CURDATE(), 'PASS', '郑国栋', 'MONTHLY', NULL, NULL, NULL, NOW()),
(2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'NEED_FIX', '郑国栋', 'MONTHLY', '1.厨房煤气报警器电池电量低需更换\n2.卫生间扶手螺丝松动需加固', 'pending', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'PASS', '周丽华', 'MONTHLY', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =====================================================
-- 26. 补充数据（管理主页图表 / 跌倒预警 / 步态趋势 / 专家咨询 / 账号权限 / 康养日程 / 系统概览）
-- 时间均使用相对日期，保证趋势图有近7天的数据点
-- =====================================================

-- 补充告警（处理预警列表 + 预警趋势 7 天数据点）
INSERT INTO \`alert\` (\`elder_name\`, \`elder_id\`, \`alert_type\`, \`level\`, \`description\`, \`alert_time\`, \`location\`, \`status\`, \`handler_id\`, \`handler\`, \`handle_time\`, \`contact_phone\`, \`emergency_contact\`, \`suggested_action\`, \`read\`, \`handle_note\`, \`create_time\`) VALUES
('赵玉兰', 4, 'FALL', 'high', '老人在卫生间门口滑倒，手环检测到异常加速度', DATE_SUB(NOW(), INTERVAL 1 DAY), 'A栋302室卫生间', 'pending', NULL, NULL, NULL, '13900004444', '赵小军（儿子）', '立即电话确认老人状况，安排护工上门查看', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('刘德胜', 5, 'DEVICE_OFFLINE', 'medium', 'GPS定位器离线超过1小时，无法获取位置', DATE_SUB(NOW(), INTERVAL 2 DAY), 'B栋401室', 'pending', NULL, NULL, NULL, '13900005555', '刘芳（女儿）', '检查定位器电量，联系家属确认老人位置', 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('陈阿妹', 6, 'ABNORMAL_GAIT', 'medium', '步态分析显示步速明显下降，跌倒风险升高', DATE_SUB(NOW(), INTERVAL 3 DAY), 'C栋502室', 'processing', 2, '张三', NULL, '13900006666', '陈明（儿子）', '安排康复师上门评估步态，调整助行器', 1, '已通知康复师上门', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('张建国', 1, 'EMERGENCY', 'high', '老人按下紧急呼叫按钮', DATE_SUB(NOW(), INTERVAL 4 DAY), 'A栋101室', 'resolved', 2, '张三', DATE_SUB(NOW(), INTERVAL 4 DAY), '13900001111', '张小明（儿子）', '立即上门确认，老人误触已安抚', 0, '误触已确认安全', DATE_SUB(NOW(), INTERVAL 4 DAY)),
('李秀英', 2, 'DEVICE_OFFLINE', 'low', '手环电量低即将关机', DATE_SUB(NOW(), INTERVAL 5 DAY), 'B栋203室', 'pending', NULL, NULL, NULL, '13900002222', '李大明（儿子）', '提醒家属为手环充电', 0, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 补充步态记录（步态趋势 7 个数据点）
INSERT INTO \`gait_record\` (\`name\`, \`elder_id\`, \`date\`, \`speed\`, \`frequency\`, \`step_length\`, \`score\`, \`result\`) VALUES
('张建国', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 0.85, 98, 54, 76, 'NORMAL'),
('张建国', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 0.79, 92, 50, 70, 'BORDERLINE'),
('张建国', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 0.88, 100, 56, 80, 'NORMAL'),
('张建国', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 0.75, 88, 48, 65, 'BORDERLINE');

-- 补充咨询（专家统计：今日咨询/待分配/进行中/满意度）
INSERT INTO \`consult\` (\`elder_name\`, \`type\`, \`expert_id\`, \`expert_name\`, \`status\`, \`urgency\`, \`consult_time\`, \`content\`, \`reply\`, \`rating\`, \`feedback\`, \`create_time\`) VALUES
('张建国', '健康咨询', 1, '陈伟明', 'completed', 'urgent', DATE_SUB(NOW(), INTERVAL 3 HOUR), '老人晨起血压偏高，头晕，咨询是否需要调整用药', '建议连续3天早晚测量血压记录，若持续偏高需面诊调整', 5, '回复及时，建议很实用', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('李秀英', '康复建议', 3, '黄志强', 'completed', 'important', DATE_SUB(NOW(), INTERVAL 2 HOUR), '膝关节术后恢复期训练方案咨询', '建议每日20分钟股四头肌训练，循序渐进，注意保暖', 4, '方案详细可执行', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('赵玉兰', '用药指导', 2, '林美丽', 'pending', 'normal', DATE_SUB(NOW(), INTERVAL 1 HOUR), '多种药物同时服用是否有冲突', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 补充用户（账号权限列表）
INSERT INTO \`user\` (\`username\`, \`password\`, \`real_name\`, \`role\`, \`status\`, \`community\`, \`account\`, \`phone\`, \`email\`, \`last_login\`, \`create_time\`) VALUES
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'staff', 'ACTIVE', '西城社区', 'wangwu', '13800003333', 'wangwu@example.com', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 30 DAY)),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 'staff', 'ACTIVE', '朝阳社区', 'zhaoliu', '13800004444', 'zhaoliu@example.com', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
('sunqi', 'e10adc3949ba59abbe56e057f20f883e', '孙七', 'admin', 'ACTIVE', '丰台社区', 'sunqi', '13800005555', 'sunqi@example.com', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 补充康养日程（今日 5 条 + 未来几天）
INSERT INTO \`schedule_task\` (\`task_code\`, \`task_type\`, \`elder_name\`, \`location\`, \`priority\`, \`task_status\`, \`trigger_reson\`, \`relate_alert_code\`, \`relate_device_sn\`, \`handle_result\`, \`handler\`, \`complete_time\`, \`note\`, \`task_date\`, \`create_time\`) VALUES
('TASK-2024004', 'HEALTH_CHECK', '赵玉兰', 'A栋302室', 'urgent', 'pending', '高血压日常随访', NULL, NULL, NULL, '周丽华', NULL, '测量血压并记录，观察用药反应', CURDATE(), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('TASK-2024005', 'REHAB_TRAINING', '张建国', 'A栋101室', 'important', 'processing', '跌倒后康复训练第2周', NULL, NULL, NULL, '郑国栋', NULL, '下肢力量训练30分钟，评估恢复进度', CURDATE(), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('TASK-2024009', 'MEDICATION', '王德发', 'C栋302室', 'important', 'pending', '降压药服用提醒与血压复测', NULL, NULL, NULL, '郑国栋', NULL, '监督服药并测量血压记录', CURDATE(), NOW()),
('TASK-2024010', 'NUTRITION', '陈阿妹', 'C栋502室', 'normal', 'pending', '营养餐配送与进食情况确认', NULL, NULL, NULL, '吴晓芳', NULL, '确认三餐营养搭配与进食量', CURDATE(), NOW()),
('TASK-2024011', 'PSYCHOLOGY', '李秀英', 'B栋203室', 'medium', 'pending', '情绪关怀与心理疏导', NULL, NULL, NULL, '吴晓芳', NULL, '陪同聊天缓解焦虑情绪', CURDATE(), NOW()),
('TASK-2024006', 'DAILY_VISIT', '李秀英', 'B栋203室', 'normal', 'pending', '认知障碍日常巡视', NULL, NULL, NULL, '吴晓芳', NULL, '确认老人状态，检查走失风险', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('TASK-2024007', 'DEVICE_REPAIR', '陈阿妹', 'C栋502室', 'medium', 'pending', '轮椅扶手松动需维修', NULL, NULL, NULL, '周丽华', NULL, '联系维修人员处理轮椅扶手', DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('TASK-2024008', 'ENV_INSPECT', NULL, 'A栋公共区域', 'low', 'pending', '月度环境安全检查', NULL, NULL, NULL, '郑国栋', NULL, '检查消防设施与应急通道', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- 补充报告管理操作日志（系统概览-生成报告计数）
INSERT INTO \`operation_log\` (\`operate_time\`, \`operator\`, \`operate_type\`, \`module\`, \`content\`, \`ip\`, \`status\`) VALUES
(DATE_SUB(NOW(), INTERVAL 1 HOUR), '系统管理员', 'GENERATE', '报告管理', '生成《2026年8月老人健康周报》', '192.168.1.1', 'SUCCESS'),
(DATE_SUB(NOW(), INTERVAL 3 HOUR), '张三', 'GENERATE', '报告管理', '生成《张建国康复训练月度报告》', '192.168.1.100', 'SUCCESS');

-- =====================================================
-- 27. 图表数据补充（近7天趋势满点 / 故障设备 / 老人与健康记录）
-- =====================================================

-- 补充故障/维护设备（设备故障统计有数据）
INSERT INTO \`device\` (\`device_sn\`, \`device_name\`, \`device_type\`, \`status\`, \`battery\`, \`firmware\`, \`bind_elder_name\`, \`bind_elder_id\`, \`bind_time\`, \`last_online_time\`, \`location\`, \`install_date\`, \`alert_rule\`, \`create_time\`) VALUES
('DEV-2024-007', '环境传感器-003', 'SENSOR', 'FAULT', NULL, 'v1.5.0', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 'C栋活动室', DATE_SUB(NOW(), INTERVAL 60 DAY), NULL, DATE_SUB(NOW(), INTERVAL 60 DAY)),
('DEV-2024-008', '智能摄像头-003', 'CAMERA', 'MAINTENANCE', NULL, 'v3.0.1', '王德发', 3, DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'C栋302室', DATE_SUB(NOW(), INTERVAL 40 DAY), '{"motionDetection":true}', DATE_SUB(NOW(), INTERVAL 40 DAY));

-- 在线设备最后在线时间分布到近7天（趋势图满7点）
UPDATE \`device\` SET last_online_time = DATE_SUB(NOW(), INTERVAL ((id - 1) % 7) DAY) WHERE status = 'ONLINE';

-- 补充今天与近几天的告警（告警趋势近7天满点）
INSERT INTO \`alert\` (\`elder_name\`, \`elder_id\`, \`alert_type\`, \`level\`, \`description\`, \`alert_time\`, \`location\`, \`status\`, \`handler_id\`, \`handler\`, \`handle_time\`, \`contact_phone\`, \`emergency_contact\`, \`suggested_action\`, \`read\`, \`handle_note\`, \`create_time\`) VALUES
('张建国', 1, 'ABNORMAL_GAIT', 'medium', '步态评估提示跌倒风险升高，建议复查', NOW(), 'A栋101室', 'pending', NULL, NULL, NULL, '13900001111', '张小明（儿子）', '安排步态复查与康复评估', 0, NULL, NOW()),
('王德发', 3, 'EMERGENCY', 'low', '老人反馈血压偏高，请求工作人员关注', DATE_SUB(NOW(), INTERVAL 1 HOUR), 'C栋302室', 'pending', NULL, NULL, NULL, '13900003333', '王小华（女儿）', '安排血压复测并记录', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 补充老人（人员统计更丰满）
INSERT INTO \`elder\` (\`name\`, \`gender\`, \`age\`, \`id_card\`, \`birth_date\`, \`ethnicity\`, \`phone\`, \`address\`, \`residence_type\`, \`remark\`, \`device_bound\`, \`create_time\`) VALUES
('钱淑珍', 'FEMALE', 79, '110101194704010000', '1947-04-01', '汉族', '13900007777', '北京市东城区XX胡同5号', 'ALONE', '高血压史，每日需服药', 0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
('孙启明', 'MALE', 83, '110101194310250000', '1943-10-25', '汉族', '13900008888', '北京市朝阳区幸福小区8栋', 'WITH_FAMILY', '慢阻肺，注意保暖防感冒', 0, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 补充健康记录
INSERT INTO \`health_note\` (\`elder_id\`, \`elder_name\`, \`record_time\`, \`recorder\`, \`note_type\`, \`content\`, \`importance\`, \`fallow_status\`, \`create_time\`) VALUES
(7, '钱淑珍', NOW(), '周丽华', 'BLOOD_PRESSURE', '晨起血压148/90，已提醒按时服药，建议低盐饮食', 'IMPORTANT', 'FOLLOWING', NOW()),
(8, '孙启明', DATE_SUB(NOW(), INTERVAL 2 HOUR), '吴晓芳', 'DAILY_CHECK', '日常巡视正常，呼吸平稳，建议上午户外活动', 'NORMAL', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- =====================================================
-- 28. 大屏可视化数据补充（服务人员/专家/步态/咨询/老人，各图表 7 项左右）
-- =====================================================
INSERT INTO \`expert\` (\`name\`, \`title\`, \`specialty\`, \`institution\`, \`phone\`, \`status\`, \`today_count\`, \`total_count\`, \`rating\`, \`schedule\`, \`create_time\`) VALUES
('王志远', '主治医师', '老年呼吸系统疾病', '示例市康复中心医院', '13800010004', 'ACTIVE', 2, 88, 4.55, '{"mon":"AM","thu":"PM"}', DATE_SUB(NOW(), INTERVAL 200 DAY)),
('刘慧敏', '主任医师', '老年糖尿病管理', '示例市中心医院', '13800010005', 'ACTIVE', 1, 112, 4.70, '{"tue":"AM","fri":"FULL"}', DATE_SUB(NOW(), INTERVAL 260 DAY)),
('赵国庆', '副主任医师', '老年康复医学', '示例市康复医院', '13800010006', 'ACTIVE', 0, 66, 4.48, '{"wed":"FULL"}', DATE_SUB(NOW(), INTERVAL 150 DAY)),
('孙丽华', '教授', '老年营养学', '示例市医学研究院', '13800010007', 'ACTIVE', 1, 54, 4.60, '{"sat":"AM"}', DATE_SUB(NOW(), INTERVAL 120 DAY));
INSERT INTO \`caregiver\` (\`name\`, \`age\`, \`position\`, \`phone\`, \`address\`, \`status\`, \`create_time\`) VALUES
('冯丽丽', 32, 'REHAB_NURSE', '13800006666', '北京市东城区XX路10号', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 180 DAY)),
('何晓燕', 40, 'PSYCHO_COUNSELOR', '13800006667', '北京市西城区XX小区3栋', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 120 DAY)),
('陈志强', 50, 'NURSING_SUPERVISOR', '13800006668', '北京市海淀区XX路8号', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 300 DAY)),
('徐丽萍', 29, 'CAREGIVER', '13800006669', '北京市丰台区XX街道6号', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 90 DAY));
INSERT INTO \`elder\` (\`name\`, \`gender\`, \`age\`, \`id_card\`, \`birth_date\`, \`ethnicity\`, \`phone\`, \`address\`, \`residence_type\`, \`remark\`, \`device_bound\`, \`create_time\`) VALUES
('周秀兰', 'FEMALE', 62, '110101196210010000', '1962-10-01', '汉族', '13900009901', '北京市朝阳区A小区', 'ALONE', '健康状况良好', 0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
('吴建国', 'MALE', 67, '110101195705020000', '1957-05-02', '汉族', '13900009902', '北京市海淀区B小区', 'WITH_FAMILY', '轻度高血压', 0, DATE_SUB(NOW(), INTERVAL 50 DAY)),
('郑淑英', 'FEMALE', 72, '110101195212030000', '1952-12-03', '汉族', '13900009903', '北京市西城区C小区', 'ALONE', '冠心病史', 1, DATE_SUB(NOW(), INTERVAL 40 DAY)),
('冯德昌', 'MALE', 77, '110101194701040000', '1947-01-04', '汉族', '13900009904', '北京市丰台区D小区', 'WITH_FAMILY', '2型糖尿病', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
('蒋玉兰', 'FEMALE', 86, '110101193803050000', '1938-03-05', '汉族', '13900009905', '北京市东城区E小区', 'ALONE', '行动不便', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
('沈国华', 'MALE', 89, '110101193506060000', '1935-06-06', '汉族', '13900009906', '北京市朝阳区F小区', 'NURSING_HOME', '长期卧床', 0, DATE_SUB(NOW(), INTERVAL 10 DAY));
INSERT INTO \`consult\` (\`elder_name\`, \`type\`, \`expert_id\`, \`expert_name\`, \`status\`, \`urgency\`, \`consult_time\`, \`content\`, \`reply\`, \`rating\`, \`feedback\`, \`create_time\`) VALUES
('周秀兰', '健康咨询', 5, '刘慧敏', 'completed', 'normal', DATE_SUB(NOW(), INTERVAL 5 DAY), '血糖控制咨询', '建议低糖饮食并每日监测', 4, '有帮助', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('吴建国', '康复建议', 6, '赵国庆', 'completed', 'normal', DATE_SUB(NOW(), INTERVAL 6 DAY), '肩部康复训练咨询', '建议每日肩部拉伸', 5, '很好', DATE_SUB(NOW(), INTERVAL 6 DAY)),
('郑淑英', '用药指导', 4, '王志远', 'completed', 'important', DATE_SUB(NOW(), INTERVAL 4 DAY), '冠心病用药咨询', '建议按医嘱规律服药', 4, '清晰', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- =====================================================
-- 填充完成
-- =====================================================
