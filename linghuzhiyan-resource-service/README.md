# 灵狐智验资源管理微服务

## 简介

资源管理微服务负责处理系统中所有文件资源的管理，包括：

- 实验资源文件管理
- 学生提交文件管理
- 公共资源管理
- 文件上传下载
- 文件存储（基于MinIO）

## 功能特性

### 核心功能

1. **文件上传管理**
   - 支持多种文件格式
   - 自动文件类型检测
   - 文件大小限制
   - 文件名安全检查

2. **存储管理**
   - 基于MinIO对象存储
   - 分桶存储策略
   - 预签名URL生成
   - 文件访问权限控制

3. **资源分类**
   - 实验资源
   - 学生提交
   - 公共资源
   - 多媒体文件支持

4. **权限控制**
   - 基于JWT的用户认证
   - 角色权限管理
   - 文件访问权限

### API功能

- 文件上传/下载
- 资源列表查询
- 资源搜索
- 热门资源统计
- 下载次数统计

## 技术栈

- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **数据库**: MySQL + Spring Data JPA
- **存储**: MinIO对象存储
- **服务发现**: Eureka Client
- **配置管理**: Spring Cloud Config

## 数据库设计

### 资源表 (resources)

```sql
CREATE TABLE resources (
    id VARCHAR(50) PRIMARY KEY,
    experiment_id VARCHAR(50),
    resource_type VARCHAR(20) NOT NULL,
    resource_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    description VARCHAR(500),
    uploader VARCHAR(50),
    is_public BOOLEAN DEFAULT TRUE,
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## MinIO存储结构

```
linghuzhiyan/           # 默认桶
├── avatars/           # 用户头像
└── temp/              # 临时文件

resource/              # 资源桶
├── experiments/       # 实验资源
│   └── {experiment_id}/
│       ├── resource/  # 学习资料
│       └── experiment/# 实验文件
├── document/          # 文档文件
├── video/            # 视频文件
├── audio/            # 音频文件
├── image/            # 图片文件
├── archive/          # 压缩包
├── code/             # 代码文件
└── other/            # 其他文件

submission/           # 提交桶
└── submissions/      # 学生提交
    └── {student_id}/
        └── {experiment_id}/
            └── {task_id}/
```

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8086

spring:
  application:
    name: resource-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/linghuzhiyan_resource
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  default-bucket: linghuzhiyan
  resource-bucket: resource
  submission-bucket: submission

jwt:
  secret: linghuzhiyan-jwt-secret-key-for-resource-service
  expiration: 86400
```

## API文档

### 资源管理接口

#### 上传资源文件
```
POST /api/v1/resources/upload
```

#### 上传学生提交文件
```
POST /api/v1/resources/submission
```

#### 获取资源详情
```
GET /api/v1/resources/{id}
```

#### 下载资源
```
GET /api/v1/resources/download/{id}
```

#### 获取实验资源列表
```
GET /api/v1/resources/experiment/{experimentId}
```

#### 获取学生提交列表
```
GET /api/v1/resources/submissions/my
```

#### 查询资源
```
POST /api/v1/resources/query
```

#### 搜索资源
```
GET /api/v1/resources/search?keyword={keyword}
```

## 部署指南

### 本地开发

1. 启动MySQL数据库
2. 启动MinIO服务
3. 启动Eureka服务发现
4. 启动Config Server
5. 运行资源服务

```bash
mvn spring-boot:run
```

### Docker部署

```bash
# 构建镜像
mvn clean package
docker build -t linghuzhiyan-resource-service .

# 运行容器
docker run -d \
  --name resource-service \
  -p 8086:8086 \
  --link mysql:mysql \
  --link minio:minio \
  linghuzhiyan-resource-service
```

## 依赖服务

- **Eureka Server**: 服务注册发现
- **Config Server**: 配置管理
- **MySQL**: 数据存储
- **MinIO**: 文件存储
- **Gateway**: API网关

## 监控与日志

- **健康检查**: `/actuator/health`
- **服务信息**: `/actuator/info`
- **指标监控**: `/actuator/metrics`

## 常见问题

### 文件上传失败
1. 检查文件大小是否超限
2. 检查MinIO服务状态
3. 检查存储桶权限

### 下载失败
1. 检查资源ID是否正确
2. 检查用户权限
3. 检查MinIO连接

### 数据库连接失败
1. 检查数据库服务状态
2. 检查连接配置
3. 检查数据库权限

## 版本历史

- v1.0.0: 初始版本，基础资源管理功能
