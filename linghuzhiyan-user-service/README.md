# 用户服务 (linghuzhiyan-user-service)

## 概述

用户服务是灵狐智验微服务架构中的核心业务服务，负责用户信息管理、资料维护、头像上传等功能。

## 功能特性

### 核心功能
- **用户注册**: 新用户注册，自动分配学生角色
- **用户信息管理**: 查询、更新用户基本信息
- **密码管理**: 用户密码修改
- **头像管理**: 头像上传到MinIO对象存储
- **用户资料**: 个人资料的结构化存储和管理
- **用户删除**: 软删除机制（管理员权限）
- **分页查询**: 支持用户列表的分页查询

### 权限管理
- **角色分配**: 通过认证服务进行角色管理
- **权限控制**: 基于角色的访问控制
- **权限层级**: 管理员 > 教师 > 助教 > 学生

## 技术栈

- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL (JPA/Hibernate)
- **对象存储**: MinIO
- **服务发现**: Eureka Client
- **配置管理**: Spring Cloud Config
- **安全认证**: Spring Security + JWT
- **服务调用**: OpenFeign
- **文档**: SpringDoc OpenAPI

## API 端点

### 用户管理
```
POST   /api/users/register              # 用户注册
GET    /api/users/profile               # 获取个人资料
PUT    /api/users/profile               # 更新个人资料
PUT    /api/users/password              # 修改密码
DELETE /api/users/delete/{userId}       # 删除用户（管理员）
GET    /api/users/{id}                  # 获取指定用户
GET    /api/users                       # 分页查询用户
```

### 头像管理
```
POST   /api/users/avatar                # 上传头像
GET    /api/users/avatar/{userId}       # 获取头像URL
```

### 角色管理
```
POST   /api/users/setrole               # 设置用户角色
GET    /api/users/{userId}/roles        # 获取用户角色
```

## 数据模型

### User 实体
```java
- id: String (UUID)           # 用户ID
- username: String            # 用户名
- email: String               # 邮箱
- password: String            # 加密密码
- avatar: String              # 头像路径
- profile: String (JSON)      # 个人资料
- createdAt: Date             # 创建时间
- updatedAt: Date             # 更新时间
- isDeleted: Boolean          # 软删除标记
```

### ProfileRequestDTO 结构
```java
- realName: String            # 真实姓名
- nickname: String            # 昵称
- gender: String              # 性别
- birthdate: String           # 出生日期
- bio: String                 # 个人简介
- location: String            # 所在地区
- phone: String               # 联系电话
- wechat: String              # 微信号
- education: String           # 教育背景
- school: String              # 学校
- major: String               # 专业
- interests: String           # 兴趣爱好
- skills: String              # 技能特长
```

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://10.128.55.225:3306/linghuzhiyan_user
    username: keyan
    password: lop*123456lop*
```

### MinIO配置
```yaml
minio:
  endpoint: http://10.128.54.190:9000
  accessKey: keyan
  secretKey: lop*123456lop*
  bucketName: linghuzhiyan
```

### 服务配置
```yaml
server:
  port: 8082

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 依赖服务

### 认证服务 (linghuzhiyan-auth-service)
- 用户角色管理
- JWT令牌验证
- 权限控制

### 基础设施服务
- Eureka Server (服务发现)
- Config Server (配置管理)
- Gateway (API网关)

## 部署说明

### 启动顺序
1. Eureka Server (8761)
2. Config Server (8888)
3. Auth Service (8081)
4. User Service (8082)
5. Gateway (8080)

### 环境要求
- JDK 21+
- MySQL 8.0+
- MinIO Server
- 网络连通性确保各服务间可互相访问

### 验证部署
1. 检查服务注册: http://localhost:8761
2. 健康检查: http://localhost:8082/actuator/health
3. API文档: http://localhost:8082/swagger-ui.html
4. 通过网关访问: http://localhost:8080/api/users/...

## 监控和日志

### 健康检查
- Actuator端点暴露健康状态
- 数据库连接状态监控
- MinIO连接状态监控

### 日志配置
- 彩色控制台输出
- 详细的SQL日志（开发环境）
- 安全相关操作日志
- 文件上传操作日志

## 安全考虑

### 密码安全
- BCrypt加密存储
- 强密码策略验证

### 文件上传安全
- 文件类型验证
- 文件大小限制（5MB）
- 恶意文件检测

### 权限控制
- 基于角色的访问控制
- 方法级权限注解
- 用户操作审计

## 扩展性

### 水平扩展
- 无状态设计
- 支持多实例部署
- 负载均衡友好

### 功能扩展
- 插件化的用户资料字段
- 可配置的头像存储策略
- 灵活的角色权限体系
