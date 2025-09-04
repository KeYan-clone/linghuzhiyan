-- Auth Service 初始化数据
-- 插入用户角色关系数据

-- 插入管理员角色
INSERT INTO user_roles (user_id, role, created_at) 
VALUES ('eb1a0386-1156-4264-b82c-0030a1021452', 'ROLE_ADMIN', NOW())
ON DUPLICATE KEY UPDATE created_at = NOW();

-- 插入第二个管理员角色
INSERT INTO user_roles (user_id, role, created_at) 
VALUES ('694c8cbb-f817-4064-a66b-e52069326143', 'ROLE_ADMIN', NOW())
ON DUPLICATE KEY UPDATE created_at = NOW();
