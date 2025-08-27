-- 插入测试数据
INSERT INTO users (id, username, email, password, avatar, profile, created_at, updated_at, is_deleted) VALUES
('test-user-1', 'testuser1', 'test1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '/avatars/test1.jpg', '{"nickname":"测试用户1"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('test-user-2', 'testuser2', 'test2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '/avatars/test2.jpg', '{"nickname":"测试用户2"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('test-user-3', 'deleteduser', 'deleted@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', NULL, '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('test-user-4', 'adminuser', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', NULL, '{"nickname":"管理员"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);
