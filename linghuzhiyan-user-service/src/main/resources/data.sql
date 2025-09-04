-- User Service 初始化数据
-- 插入管理员用户

-- 插入管理员用户1
INSERT INTO users (id, username, email, password, avatar, profile, created_at, updated_at, is_deleted) 
VALUES (
    'eb1a0386-1156-4264-b82c-0030a1021452', 
    'user1', 
    'user1@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    NULL,
    '{}',
    NOW(), 
    NOW(),
    FALSE
) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 插入管理员用户2
INSERT INTO users (id, username, email, password, avatar, profile, created_at, updated_at, is_deleted) 
VALUES (
    '694c8cbb-f817-4064-a66b-e52069326143', 
    'admin', 
    'admin@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    NULL,
    '{}',
    NOW(), 
    NOW(),
    FALSE
) ON DUPLICATE KEY UPDATE updated_at = NOW();
