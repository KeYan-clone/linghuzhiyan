-- 创建资源表
DROP TABLE IF EXISTS resource;

CREATE TABLE resource (
    id VARCHAR(36) PRIMARY KEY,
    experiment_id VARCHAR(36),
    resource_type ENUM('DOCUMENT', 'IMAGE', 'VIDEO', 'CODE', 'OTHER', 'SUBMISSION', 'PRESENTATION', 'SPREADSHEET', 'AUDIO', 'ARCHIVE') NOT NULL,
    resource_path VARCHAR(255) NOT NULL,
    file_name VARCHAR(100),
    file_size BIGINT,
    mime_type VARCHAR(50),
    description TEXT,
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_resource_experiment_id ON resource(experiment_id);
CREATE INDEX idx_resource_type ON resource(resource_type);
CREATE INDEX idx_resource_upload_time ON resource(upload_time);
