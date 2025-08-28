-- 测试数据插入
INSERT INTO resource (id, experiment_id, resource_type, resource_path, file_name, file_size, mime_type, description, upload_time) VALUES
('test-resource-1', 'exp-001', 'DOCUMENT', '/path/to/test1.pdf', 'test1.pdf', 1024, 'application/pdf', '测试文档1', '2023-01-01 10:00:00'),
('test-resource-2', 'exp-001', 'IMAGE', '/path/to/test2.jpg', 'test2.jpg', 2048, 'image/jpeg', '测试图片1', '2023-01-01 11:00:00'),
('test-resource-3', 'exp-002', 'VIDEO', '/path/to/test3.mp4', 'test3.mp4', 10240, 'video/mp4', '测试视频1', '2023-01-01 12:00:00'),
('test-resource-4', 'exp-002', 'CODE', '/path/to/test4.java', 'test4.java', 512, 'text/plain', '测试代码1', '2023-01-01 13:00:00'),
('test-resource-5', 'exp-003', 'SUBMISSION', '/path/to/submission1.zip', 'submission1.zip', 4096, 'application/zip', '学生提交1', '2023-01-01 14:00:00'),
('test-resource-6', NULL, 'DOCUMENT', '/path/to/public.pdf', 'public.pdf', 1536, 'application/pdf', '公共文档', '2023-01-01 15:00:00');
