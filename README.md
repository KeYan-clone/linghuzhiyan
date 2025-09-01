# 灵狐智验微服务架构项目

## 项目概述

灵狐智验是一个基于Spring Cloud微服务架构的教学实验管理系统，从单体应用重构为微服务架构。

## 项目结构

```
linghuzhiyan-micro/
├── pom.xml                           # 父项目POM
├── config-repo/                      # 配置文件存储
│   ├── application.yml              # 通用配置
│   ├── gateway.yml                  # 网关配置
│   ├── auth-service.yml             # 认证服务配置
│   ├── user-service.yml             # 用户服务配置
│   └── monitor-service.yml          # 监控服务配置
├── linghuzhiyan-discovery-server/    # 服务发现中心
├── linghuzhiyan-config-server/       # 配置中心
├── linghuzhiyan-gateway/             # API网关
├── linghuzhiyan-monitor-service/     # 监控服务
├── linghuzhiyan-common/              # 公共模块
└── [业务服务模块...]
```

## 基础设施服务

### 1. 服务发现中心 (Eureka Server) - 端口8761
- 负责微服务注册与发现
- 访问地址: http://localhost:8761
- 登录账号: admin/admin123

### 2. 配置中心 (Config Server) - 端口8888
- 集中管理所有微服务配置
- 访问地址: http://localhost:8888
- 登录账号: config/config123

### 3. API网关 (Spring Cloud Gateway) - 端口8080
- 统一API入口，路由转发
- JWT认证，跨域处理
- 访问地址: http://localhost:8080

### 4. 监控服务 (Spring Boot Admin) - 端口8090
- 微服务健康状态监控
- 访问地址: http://localhost:8090
- 登录账号: admin/admin123

## 快速启动

### 方法一：使用启动脚本（推荐）

**Windows:**
```batch
start-infrastructure.bat
```

**Linux/macOS:**
```bash
chmod +x start-infrastructure.sh
./start-infrastructure.sh
```

### 方法二：手动启动

按以下顺序启动基础设施服务：

1. **启动服务发现中心**
```bash
cd linghuzhiyan-discovery-server
mvn spring-boot:run
```

2. **启动配置中心**
```bash
cd linghuzhiyan-config-server
mvn spring-boot:run
```

3. **启动API网关**
```bash
cd linghuzhiyan-gateway
mvn spring-boot:run
```

4. **启动监控服务**
```bash
cd linghuzhiyan-monitor-service
mvn spring-boot:run
```

## API文档 (Swagger)

项目集成了SpringDoc OpenAPI 3.0，提供完整的API文档和在线测试功能。

### 全局访问
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API配置**: http://localhost:8080/v3/api-docs/swagger-config

### 服务独立访问
| 服务 | 端口 | Swagger UI |
|------|------|------------|
| 用户服务 | 8082 | http://localhost:8082/swagger-ui.html |
| 认证服务 | 8084 | http://localhost:8084/swagger-ui.html |
| 实验服务 | 8083 | http://localhost:8083/swagger-ui.html |
| 资源服务 | 8085 | http://localhost:8085/swagger-ui.html |
| 消息服务 | 8086 | http://localhost:8086/swagger-ui.html |
| 讨论服务 | 8087 | http://localhost:8087/swagger-ui.html |

### Swagger启动和测试
```powershell
# 启动所有服务并测试Swagger
.\start-swagger-demo.ps1

# 测试Swagger文档可访问性
.\test-swagger.ps1
```

详细说明请参考: [SWAGGER_README.md](./SWAGGER_README.md)

## 验证启动

所有服务启动后，可以通过以下方式验证：

1. **Eureka控制台**: http://localhost:8761
   - 应该能看到所有注册的服务

2. **配置中心**: http://localhost:8888/application/default
   - 应该能返回配置信息

3. **监控服务**: http://localhost:8090
   - 应该能看到所有服务的健康状态

## 注意事项

1. **启动顺序很重要**: 必须先启动服务发现中心，然后是配置中心，最后是其他服务
2. **端口冲突**: 确保8761、8888、8080、8090端口没有被占用
3. **数据库连接**: 确保MySQL数据库(10.128.54.190:3306)可以正常连接
4. **Java版本**: 项目使用Java 21，请确保本地环境正确

## 下一步

基础设施搭建完成后，可以按照迁移策略继续部署业务服务：

1. 认证服务 (linghuzhiyan-auth-service)
2. 用户服务 (linghuzhiyan-user-service)
3. 实验服务 (linghuzhiyan-experiment-service)
4. 其他业务服务...

## 技术栈

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Gateway
- Netflix Eureka
- Spring Cloud Config
- Spring Boot Admin
- Java 21
- Maven
