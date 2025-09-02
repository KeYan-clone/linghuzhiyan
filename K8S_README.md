# Kubernetes 部署指南

## 概述

本文档描述如何将灵狐智验微服务架构部署到Kubernetes集群中。所有配置文件都位于 `k8s/` 目录下。

## 前置条件

### 系统要求
- Kubernetes 1.20+
- kubectl 客户端工具
- 至少 4GB 可用内存
- 至少 20GB 可用存储空间

### 环境准备
```bash
# 检查kubectl是否正确配置
kubectl cluster-info

# 检查节点状态
kubectl get nodes

# 检查存储类（如果使用动态存储）
kubectl get storageclass
```

## 部署架构

### 命名空间
所有服务部署在 `linghuzhiyan` 命名空间中，提供资源隔离和管理。

### 服务组件

#### 基础设施层
- **MySQL 8.0** - 持久化数据库，使用PVC存储
- **Redis 7** - 缓存服务
- **MinIO** - 对象存储服务，使用PVC存储

#### 微服务层
- **Discovery Server** - 服务注册与发现
- **Config Server** - 配置管理中心
- **Gateway** - API网关，对外暴露服务
- **业务服务** - Auth, User, Experiment, Resource, Message, Discussion

### 网络配置
- 使用ClusterIP进行内部服务通信
- 使用LoadBalancer或NodePort对外暴露Gateway
- 支持Ingress配置自定义域名

## 快速部署

### 方法：分步部署

```bash
# 1. 创建命名空间
kubectl apply -f k8s/namespace.yaml

# 2. 部署基础设施
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/minio.yaml

# 3. 等待基础设施就绪
kubectl wait --for=condition=Ready pod -l app=mysql -n linghuzhiyan --timeout=300s

# 4. 部署核心服务
kubectl apply -f k8s/discovery-server.yaml
kubectl wait --for=condition=Ready pod -l app=discovery-server -n linghuzhiyan --timeout=300s

kubectl apply -f k8s/config-server.yaml
kubectl wait --for=condition=Ready pod -l app=config-server -n linghuzhiyan --timeout=300s

kubectl apply -f k8s/gateway.yaml

# 5. 部署业务服务
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/user-service.yaml
kubectl apply -f k8s/experiment-service.yaml
kubectl apply -f k8s/resource-service.yaml
kubectl apply -f k8s/message-service.yaml
kubectl apply -f k8s/discussion-service.yaml
```

## 镜像管理

### 构建镜像

```bash
# 构建所有服务镜像
docker build -t linghuzhiyan/discovery-server:latest -f linghuzhiyan-discovery-server/Dockerfile .
docker build -t linghuzhiyan/config-server:latest -f linghuzhiyan-config-server/Dockerfile .
docker build -t linghuzhiyan/gateway:latest -f linghuzhiyan-gateway/Dockerfile .
docker build -t linghuzhiyan/auth-service:latest -f linghuzhiyan-auth-service/Dockerfile .
docker build -t linghuzhiyan/user-service:latest -f linghuzhiyan-user-service/Dockerfile .
docker build -t linghuzhiyan/experiment-service:latest -f linghuzhiyan-experiment-service/Dockerfile .
docker build -t linghuzhiyan/resource-service:latest -f linghuzhiyan-resource-service/Dockerfile .
docker build -t linghuzhiyan/message-service:latest -f linghuzhiyan-message-service/Dockerfile .
docker build -t linghuzhiyan/discussion-service:latest -f linghuzhiyan-discussion-service/Dockerfile .
```

### 推送到镜像仓库

```bash
# 标记镜像（替换为您的仓库地址）
docker tag linghuzhiyan/gateway:latest your-registry.com/linghuzhiyan/gateway:latest

# 推送镜像
docker push your-registry.com/linghuzhiyan/gateway:latest
```

## 服务访问

### 内部访问
服务间通过服务名进行通信：
- mysql:3306
- redis:6379
- minio:9000
- discovery-server:8761
- config-server:8888
- gateway:8080

### 外部访问

#### LoadBalancer方式
```bash
# 获取外部IP
kubectl get service gateway-external -n linghuzhiyan

# 访问应用
curl http://<EXTERNAL-IP>/actuator/health
```

#### NodePort方式
```bash
# 查看NodePort
kubectl get service discovery-server-external -n linghuzhiyan

# 访问服务发现
http://<NODE-IP>:<NODE-PORT>
```

#### 端口转发方式
```bash
# 转发网关端口
kubectl port-forward service/gateway 8080:8080 -n linghuzhiyan

# 本地访问
http://localhost:8080
http://localhost:8080/swagger-ui.html
```

#### Ingress方式
```bash
# 配置hosts文件
echo "127.0.0.1 api.linghuzhiyan.local" >> /etc/hosts

# 访问应用
http://api.linghuzhiyan.local
```

## 监控和运维

### 查看资源状态

```bash
# 查看所有资源
kubectl get all -n linghuzhiyan

# 查看Pod详细信息
kubectl describe pod <pod-name> -n linghuzhiyan

# 查看服务端点
kubectl get endpoints -n linghuzhiyan
```

### 日志管理

```bash
# 查看Pod日志
kubectl logs <pod-name> -n linghuzhiyan

# 实时跟踪日志
kubectl logs -f <pod-name> -n linghuzhiyan

# 查看所有容器日志
kubectl logs <pod-name> --all-containers -n linghuzhiyan
```

### 健康检查

```bash
# 检查Pod健康状态
kubectl get pods -n linghuzhiyan -o wide

# 检查服务健康
kubectl get endpoints -n linghuzhiyan

# 通过网关检查服务
curl http://<gateway-url>/actuator/health
```

### 扩缩容

```bash
# 扩展Gateway副本
kubectl scale deployment gateway --replicas=3 -n linghuzhiyan

# 扩展业务服务
kubectl scale deployment user-service --replicas=2 -n linghuzhiyan

# 查看扩容状态
kubectl get deployment -n linghuzhiyan
```

## 配置管理

### 环境变量配置
通过Deployment中的env字段配置环境变量：

```yaml
env:
- name: DB_HOST
  value: "mysql"
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: mysql-secret
      key: password
```

### Secret管理
```bash
# 创建数据库密码Secret
kubectl create secret generic mysql-secret \
  --from-literal=password=your-password \
  -n linghuzhiyan

# 查看Secret
kubectl get secrets -n linghuzhiyan

# 更新Secret
kubectl patch secret mysql-secret -n linghuzhiyan \
  -p='{"data":{"password":"bmV3LXBhc3N3b3Jk"}}'
```

### ConfigMap配置
```bash
# 创建配置文件ConfigMap
kubectl create configmap app-config \
  --from-file=config-repo/ \
  -n linghuzhiyan

# 查看ConfigMap
kubectl get configmap -n linghuzhiyan

# 查看ConfigMap内容
kubectl describe configmap app-config -n linghuzhiyan
```

## 持久化存储

### PVC管理
```bash
# 查看PVC状态
kubectl get pvc -n linghuzhiyan

# 查看PV信息
kubectl get pv

# 扩展PVC（如果存储类支持）
kubectl patch pvc mysql-pvc -n linghuzhiyan \
  -p='{"spec":{"resources":{"requests":{"storage":"20Gi"}}}}'
```

### 备份策略
```bash
# 备份MySQL数据
kubectl exec mysql-xxx -n linghuzhiyan -- \
  mysqldump -u root -p --all-databases > backup.sql

# 备份MinIO数据
kubectl cp minio-xxx:/data ./minio-backup -n linghuzhiyan
```

## 故障排除

### 常见问题

#### Pod启动失败
```bash
# 查看Pod事件
kubectl describe pod <pod-name> -n linghuzhiyan

# 查看Pod日志
kubectl logs <pod-name> -n linghuzhiyan --previous

# 检查镜像拉取
kubectl get events -n linghuzhiyan --sort-by='.lastTimestamp'
```

#### 服务连接失败
```bash
# 检查服务发现
kubectl get endpoints -n linghuzhiyan

# 测试服务连接
kubectl run test-pod --image=busybox -it --rm -n linghuzhiyan -- \
  wget -qO- http://gateway:8080/actuator/health
```

#### 存储问题
```bash
# 检查存储类
kubectl get storageclass

# 检查PV状态
kubectl get pv

# 检查节点存储
kubectl describe node <node-name>
```

### 调试工具

#### 进入容器
```bash
# 进入运行中的容器
kubectl exec -it <pod-name> -n linghuzhiyan -- /bin/bash

# 运行临时调试Pod
kubectl run debug --image=busybox -it --rm -n linghuzhiyan -- sh
```

#### 网络调试
```bash
# 检查DNS解析
kubectl exec -it <pod-name> -n linghuzhiyan -- nslookup gateway

# 检查端口连通性
kubectl exec -it <pod-name> -n linghuzhiyan -- telnet gateway 8080
```

## 升级部署

### 滚动更新
```bash
# 更新镜像
kubectl set image deployment/gateway gateway=linghuzhiyan/gateway:v2.0 -n linghuzhiyan

# 查看更新状态
kubectl rollout status deployment/gateway -n linghuzhiyan

# 查看更新历史
kubectl rollout history deployment/gateway -n linghuzhiyan
```

### 回滚部署
```bash
# 回滚到上个版本
kubectl rollout undo deployment/gateway -n linghuzhiyan

# 回滚到指定版本
kubectl rollout undo deployment/gateway --to-revision=2 -n linghuzhiyan
```

## 安全配置

### RBAC配置
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: linghuzhiyan
  name: linghuzhiyan-role
rules:
- apiGroups: [""]
  resources: ["pods", "services"]
  verbs: ["get", "list", "watch"]
```

### 网络策略
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: linghuzhiyan-netpol
  namespace: linghuzhiyan
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
```

## 性能优化

### 资源配置
- 根据实际负载调整CPU和内存限制
- 配置合适的副本数
- 使用HPA进行自动扩缩容

### JVM调优
```yaml
env:
- name: JAVA_OPTS
  value: "-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 存储优化
- 使用SSD存储类
- 配置合适的存储大小
- 考虑使用分布式存储

## 生产环境建议

1. **高可用部署**
   - 多副本部署
   - 跨节点调度
   - 健康检查配置

2. **监控告警**
   - 集成Prometheus
   - 配置Grafana仪表板
   - 设置告警规则

3. **日志管理**
   - 集成ELK Stack
   - 配置日志采集
   - 设置日志轮转

4. **备份策略**
   - 定期数据备份
   - 配置文件备份
   - 灾难恢复计划

## 参考资料

- [Kubernetes官方文档](https://kubernetes.io/docs/)
- [Spring Boot Kubernetes部署指南](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [项目Docker部署文档](./DOCKER_README.md)
- [项目主要文档](./README.md)
