-- 添加系统设置相关的配置项
USE canteen_system;

-- 插入系统设置相关配置
INSERT INTO system_config (config_key, config_value, config_type, description, is_public) VALUES
('system.maintenance_mode', 'false', 'BOOLEAN', 'System maintenance mode switch', 0),
('system.allow_registration', 'true', 'BOOLEAN', 'Allow user registration', 0),
('system.announcement', 'Welcome to Campus Canteen System!', 'STRING', 'System announcement', 1),
('system.max_upload_size', '10', 'INTEGER', 'Max upload file size in MB', 0),
('system.session_timeout', '30', 'INTEGER', 'Session timeout in minutes', 0)
ON DUPLICATE KEY UPDATE 
config_value = VALUES(config_value),
description = VALUES(description),
is_public = VALUES(is_public);

-- 确保有管理员用户的设置数据
INSERT IGNORE INTO user_notification_settings (user_id, email_notification, sms_notification, order_notification, promotion_notification, system_notification) 
SELECT id, 1, 1, 1, 1, 1 FROM user WHERE role = 'ADMIN';

INSERT IGNORE INTO user_preference_settings (user_id, language, theme, timezone, auto_login, page_size, default_payment) 
SELECT id, 'zh-CN', 'light', 'Asia/Shanghai', 0, 20, 'alipay' FROM user WHERE role = 'ADMIN';

-- 确保有商户用户的设置数据
INSERT IGNORE INTO user_notification_settings (user_id, email_notification, sms_notification, order_notification, promotion_notification, system_notification) 
SELECT id, 1, 0, 1, 0, 1 FROM user WHERE role = 'MERCHANT';

INSERT IGNORE INTO user_preference_settings (user_id, language, theme, timezone, auto_login, page_size, default_payment) 
SELECT id, 'zh-CN', 'light', 'Asia/Shanghai', 0, 20, 'alipay' FROM user WHERE role = 'MERCHANT';

-- 确保有普通用户的设置数据
INSERT IGNORE INTO user_notification_settings (user_id, email_notification, sms_notification, order_notification, promotion_notification, system_notification) 
SELECT id, 1, 0, 1, 1, 1 FROM user WHERE role = 'USER';

INSERT IGNORE INTO user_preference_settings (user_id, language, theme, timezone, auto_login, page_size, default_payment) 
SELECT id, 'zh-CN', 'light', 'Asia/Shanghai', 0, 20, 'alipay' FROM user WHERE role = 'USER';