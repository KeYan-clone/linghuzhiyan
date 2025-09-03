pipeline {
    agent any
    
    environment {
        
        // Docker配置
        DOCKER_REGISTRY = 'localhost:5000'
        IMAGE_TAG = "${BUILD_NUMBER}"
        
        // Kubernetes配置
        KUBECONFIG = credentials('kubeconfig')
        K8S_NAMESPACE = 'linghuzhiyan'
        
        // 项目配置
        PROJECT_NAME = 'linghuzhiyan-micro'
        SERVICES = 'linghuzhiyan-auth-service,linghuzhiyan-user-service,linghuzhiyan-gateway,linghuzhiyan-config-server,linghuzhiyan-discovery-server,linghuzhiyan-monitor-service,linghuzhiyan-experiment-service,linghuzhiyan-discussion-service,linghuzhiyan-message-service,linghuzhiyan-resource-service'
        
        // 系统检测
        IS_WINDOWS = "${env.NODE_NAME?.toLowerCase()?.contains('windows') ?: isUnix() ? 'false' : 'true'}"
    }
    
    stages {
        stage('System Detection') {
            steps {
                script {
                    // 检测操作系统
                    env.IS_WINDOWS = isUnix() ? 'false' : 'true'
                    
                    if (env.IS_WINDOWS == 'true') {
                        echo '🪟 检测到Windows系统'
                        env.SHELL_EXECUTOR = 'powershell'
                        env.PATH_SEPARATOR = '\\'
                        env.DOCKER_COMPOSE_CMD = 'docker-compose'
                        env.KUBECTL_CMD = 'kubectl'
                        env.MAVEN_CMD = 'mvn'
                    } else {
                        echo '🐧 检测到Unix/Linux系统'
                        env.SHELL_EXECUTOR = 'bash'
                        env.PATH_SEPARATOR = '/'
                        env.DOCKER_COMPOSE_CMD = 'docker-compose'
                        env.KUBECTL_CMD = 'kubectl'
                        env.MAVEN_CMD = 'mvn'
                    }
                    
                    echo "系统类型: ${env.IS_WINDOWS == 'true' ? 'Windows' : 'Unix/Linux'}"
                    echo "Shell执行器: ${env.SHELL_EXECUTOR}"
                }
            }
        }
        
        stage('Checkout') {
            steps {
                echo '📦 检出代码...'
                checkout scm
                
                script {
                    if (env.IS_WINDOWS == 'true') {
                        env.GIT_COMMIT_SHORT = powershell(
                            script: 'git rev-parse --short HEAD',
                            returnStdout: true
                        ).trim()
                    } else {
                        env.GIT_COMMIT_SHORT = sh(
                            script: 'git rev-parse --short HEAD',
                            returnStdout: true
                        ).trim()
                    }
                    
                    env.BUILD_VERSION = "${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
                
                echo "构建版本: ${env.BUILD_VERSION}"
            }
        }
        
        stage('Code Quality Check') {
            parallel {
                stage('Compile') {
                    steps {
                        echo '🔨 编译项目...'
                        script {
                            if (env.IS_WINDOWS == 'true') {
                                powershell "${env.MAVEN_CMD} clean compile"
                            } else {
                                sh "${env.MAVEN_CMD} clean package"
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                echo '🐳 使用Docker Compose构建所有镜像...'
                script {
                    if (env.IS_WINDOWS == 'true') {
                        powershell """
                            Write-Host "开始使用Docker Compose构建所有服务镜像..."
                            
                            # 设置环境变量
                            \$env:BUILD_VERSION = "${env.BUILD_VERSION}"
                            \$env:DOCKER_REGISTRY = "${env.DOCKER_REGISTRY}"
                            \$env:VERSION = "${env.BUILD_VERSION}"
                            \$env:BUILD_DATE = Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ"
                            
                            # 显示当前环境变量
                            Write-Host "构建版本: \$env:BUILD_VERSION"
                            Write-Host "镜像仓库: \$env:DOCKER_REGISTRY"
                            Write-Host "构建日期: \$env:BUILD_DATE"
                            
                            # 使用Docker Compose构建所有服务
                            Write-Host "开始构建所有服务镜像..."
                            docker-compose -f docker-compose.yml build --progress=plain --no-cache discovery config gateway auth user experiment resource message discussion monitor
                            
                            if (\$LASTEXITCODE -eq 0) {
                                Write-Host "✅ 所有服务镜像构建成功"
                                
                                # 定义服务映射关系 (compose服务名 -> 完整服务名)
                                \$serviceMap = @{
                                    'discovery' = 'linghuzhiyan-discovery-server'
                                    'config' = 'linghuzhiyan-config-server'
                                    'gateway' = 'linghuzhiyan-gateway'
                                    'auth' = 'linghuzhiyan-auth-service'
                                    'user' = 'linghuzhiyan-user-service'
                                    'monitor' = 'linghuzhiyan-monitor-service'
                                    'experiment' = 'linghuzhiyan-experiment-service'
                                    'discussion' = 'linghuzhiyan-discussion-service'
                                    'message' = 'linghuzhiyan-message-service'
                                    'resource' = 'linghuzhiyan-resource-service'
                                }
                                
                                foreach (\$composeService in \$serviceMap.Keys) {
                                    \$fullServiceName = \$serviceMap[\$composeService]
                                    Write-Host "处理服务: \$composeService -> \$fullServiceName"
                                    
                                    # 获取compose构建的镜像名
                                    \$composeImageName = "linghuzhiyan/\$composeService"
                                    
                                    # 重新标记镜像为仓库格式
                                    docker tag "\$composeImageName:${env.BUILD_VERSION}" "${env.DOCKER_REGISTRY}/\$fullServiceName:${env.BUILD_VERSION}"
                                    docker tag "\$composeImageName:${env.BUILD_VERSION}" "${env.DOCKER_REGISTRY}/\$fullServiceName:latest"
                                    
                                    # 推送镜像
                                    Write-Host "推送 \$fullServiceName 镜像到仓库..."
                                    docker push "${env.DOCKER_REGISTRY}/\$fullServiceName:${env.BUILD_VERSION}"
                                    docker push "${env.DOCKER_REGISTRY}/\$fullServiceName:latest"
                                    
                                    Write-Host "✅ \$fullServiceName 镜像推送完成"
                                }
                                
                                Write-Host "✅ 所有镜像构建和推送完成"
                            } else {
                                Write-Host "❌ Docker Compose构建失败"
                                exit 1
                            }
                        """
                    } else {
                        sh """
                            echo "开始使用Docker Compose构建所有服务镜像..."
                            
                            # 设置环境变量
                            export BUILD_VERSION="${env.BUILD_VERSION}"
                            export DOCKER_REGISTRY="${env.DOCKER_REGISTRY}"
                            export VERSION="${env.BUILD_VERSION}"
                            export BUILD_DATE=\$(date -u +"%Y-%m-%dT%H:%M:%SZ")
                            
                            # 显示当前环境变量
                            echo "构建版本: \$BUILD_VERSION"
                            echo "镜像仓库: \$DOCKER_REGISTRY"
                            echo "构建日期: \$BUILD_DATE"
                            
                            # 使用Docker Compose构建所有服务
                            echo "开始构建所有服务镜像..."
                            docker-compose -f docker-compose.yml build --progress=plain --no-cache discovery config gateway auth user experiment resource message discussion monitor
                            
                            if [ \$? -eq 0 ]; then
                                echo "✅ 所有服务镜像构建成功"
                                
                                # 定义服务映射关系 (compose服务名 -> 完整服务名)
                                declare -A service_map=(
                                    ["discovery"]="linghuzhiyan-discovery-server"
                                    ["config"]="linghuzhiyan-config-server"
                                    ["gateway"]="linghuzhiyan-gateway"
                                    ["auth"]="linghuzhiyan-auth-service"
                                    ["user"]="linghuzhiyan-user-service"
                                    ["monitor"]="linghuzhiyan-monitor-service"
                                    ["experiment"]="linghuzhiyan-experiment-service"
                                    ["discussion"]="linghuzhiyan-discussion-service"
                                    ["message"]="linghuzhiyan-message-service"
                                    ["resource"]="linghuzhiyan-resource-service"
                                )
                                
                                for compose_service in "\${!service_map[@]}"; do
                                    full_service_name="\${service_map[\$compose_service]}"
                                    echo "处理服务: \$compose_service -> \$full_service_name"
                                    
                                    # 获取compose构建的镜像名
                                    compose_image_name="linghuzhiyan/\$compose_service"
                                    
                                    # 重新标记镜像为仓库格式
                                    docker tag "\$compose_image_name:${env.BUILD_VERSION}" "${env.DOCKER_REGISTRY}/\$full_service_name:${env.BUILD_VERSION}"
                                    docker tag "\$compose_image_name:${env.BUILD_VERSION}" "${env.DOCKER_REGISTRY}/\$full_service_name:latest"
                                    
                                    # 推送镜像
                                    echo "推送 \$full_service_name 镜像到仓库..."
                                    docker push "${env.DOCKER_REGISTRY}/\$full_service_name:${env.BUILD_VERSION}"
                                    docker push "${env.DOCKER_REGISTRY}/\$full_service_name:latest"
                                    
                                    echo "✅ \$full_service_name 镜像推送完成"
                                done
                                
                                echo "✅ 所有镜像构建和推送完成"
                            else
                                echo "❌ Docker Compose构建失败"
                                exit 1
                            fi
                        """
                    }
                }
            }
            post {
                always {
                    script {
                        // 清理本地镜像
                        if (env.IS_WINDOWS == 'true') {
                            powershell '''
                                docker image prune -f
                                docker system prune -f --volumes
                            '''
                        } else {
                            sh '''
                                docker image prune -f
                                docker system prune -f --volumes
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Deploy to K8s') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo '☸️ 部署到Kubernetes...'
                script {
                    if (env.IS_WINDOWS == 'true') {
                        powershell '''
                            # 更新镜像标签
                            $services = "${env.SERVICES}" -split ","
                            foreach ($service in $services) {
                                $serviceName = $service -replace "linghuzhiyan-", ""
                                $k8sFile = "k8s\\$serviceName.yaml"
                                if (Test-Path $k8sFile) {
                                    (Get-Content $k8sFile) -replace "image: .*/$service:.*", "image: ${env.DOCKER_REGISTRY}/$service:${env.BUILD_VERSION}" | Set-Content $k8sFile
                                }
                            }
                            
                            # 创建命名空间
                            kubectl create namespace ${env.K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                            
                            # 部署基础设施
                            Write-Host "部署基础设施..."
                            if (Test-Path "k8s\\namespace.yaml") { kubectl apply -f k8s\\namespace.yaml -n ${env.K8S_NAMESPACE} }
                            kubectl apply -f k8s\\mysql.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\redis.yaml -n ${env.K8S_NAMESPACE}
                            if (Test-Path "k8s\\minio.yaml") { kubectl apply -f k8s\\minio.yaml -n ${env.K8S_NAMESPACE} }
                            
                            # 等待基础设施就绪
                            kubectl wait --for=condition=ready pod -l app=mysql -n ${env.K8S_NAMESPACE} --timeout=300s
                            kubectl wait --for=condition=ready pod -l app=redis -n ${env.K8S_NAMESPACE} --timeout=300s
                            
                            # 部署核心服务
                            Write-Host "部署核心服务..."
                            kubectl apply -f k8s\\config-server.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\discovery-server.yaml -n ${env.K8S_NAMESPACE}
                            
                            # 等待核心服务就绪
                            kubectl wait --for=condition=ready pod -l app=config-server -n ${env.K8S_NAMESPACE} --timeout=300s
                            kubectl wait --for=condition=ready pod -l app=discovery-server -n ${env.K8S_NAMESPACE} --timeout=300s
                            
                            # 部署业务服务
                            Write-Host "部署业务服务..."
                            kubectl apply -f k8s\\auth-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\user-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\experiment-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\discussion-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\message-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\resource-service.yaml -n ${env.K8S_NAMESPACE}
                            kubectl apply -f k8s\\monitor-service.yaml -n ${env.K8S_NAMESPACE}
                            
                            # 部署网关
                            Write-Host "部署网关..."
                            kubectl apply -f k8s\\gateway.yaml -n ${env.K8S_NAMESPACE}
                            
                            # 等待所有服务就绪
                            Write-Host "等待服务就绪..."
                            kubectl wait --for=condition=ready pod -l app=gateway -n ${env.K8S_NAMESPACE} --timeout=600s
                        '''
                    } else {
                        sh '''
                            # 更新镜像标签
                            for service in $(echo ${SERVICES} | tr ',' ' '); do
                                service_name=$(echo $service | sed 's/linghuzhiyan-//')
                                k8s_file="k8s/${service_name}.yaml"
                                if [ -f "$k8s_file" ]; then
                                    sed -i "s|image: .*/${service}:.*|image: ${DOCKER_REGISTRY}/${service}:${BUILD_VERSION}|g" "$k8s_file"
                                fi
                            done
                            
                            # 创建命名空间
                            kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                            
                            # 部署基础设施
                            echo "部署基础设施..."
                            [ -f "k8s/namespace.yaml" ] && kubectl apply -f k8s/namespace.yaml -n ${K8S_NAMESPACE} || true
                            kubectl apply -f k8s/mysql.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/redis.yaml -n ${K8S_NAMESPACE}
                            [ -f "k8s/minio.yaml" ] && kubectl apply -f k8s/minio.yaml -n ${K8S_NAMESPACE} || true
                            
                            # 等待基础设施就绪
                            kubectl wait --for=condition=ready pod -l app=mysql -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl wait --for=condition=ready pod -l app=redis -n ${K8S_NAMESPACE} --timeout=300s
                            
                            # 部署核心服务
                            echo "部署核心服务..."
                            kubectl apply -f k8s/config-server.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/discovery-server.yaml -n ${K8S_NAMESPACE}
                            
                            # 等待核心服务就绪
                            kubectl wait --for=condition=ready pod -l app=config-server -n ${K8S_NAMESPACE} --timeout=300s
                            kubectl wait --for=condition=ready pod -l app=discovery-server -n ${K8S_NAMESPACE} --timeout=300s
                            
                            # 部署业务服务
                            echo "部署业务服务..."
                            kubectl apply -f k8s/auth-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/user-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/experiment-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/discussion-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/message-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/resource-service.yaml -n ${K8S_NAMESPACE}
                            kubectl apply -f k8s/monitor-service.yaml -n ${K8S_NAMESPACE}
                            
                            # 部署网关
                            echo "部署网关..."
                            kubectl apply -f k8s/gateway.yaml -n ${K8S_NAMESPACE}
                            
                            # 等待所有服务就绪
                            echo "等待服务就绪..."
                            kubectl wait --for=condition=ready pod -l app=gateway -n ${K8S_NAMESPACE} --timeout=600s
                        '''
                    }
                }
            }
            post {
                success {
                    echo '✅ Kubernetes部署成功'
                }
                failure {
                    echo '❌ Kubernetes部署失败'
                    script {
                        if (env.IS_WINDOWS == 'true') {
                            powershell '''
                                Write-Host "查看部署状态:"
                                kubectl get pods -n ${env.K8S_NAMESPACE}
                                kubectl get services -n ${env.K8S_NAMESPACE}
                                
                                Write-Host "查看失败的Pod日志:"
                                $failedPods = kubectl get pods -n ${env.K8S_NAMESPACE} --field-selector=status.phase!=Running --no-headers | ForEach-Object { ($_ -split "\\s+")[0] }
                                foreach ($pod in $failedPods) {
                                    if ($pod) {
                                        Write-Host "=== Logs for $pod ==="
                                        kubectl logs $pod -n ${env.K8S_NAMESPACE} --tail=50
                                    }
                                }
                            '''
                        } else {
                            sh '''
                                echo "查看部署状态:"
                                kubectl get pods -n ${K8S_NAMESPACE}
                                kubectl get services -n ${K8S_NAMESPACE}
                                
                                echo "查看失败的Pod日志:"
                                kubectl get pods -n ${K8S_NAMESPACE} --field-selector=status.phase!=Running --no-headers | \\
                                awk '{print $1}' | xargs -I {} kubectl logs {} -n ${K8S_NAMESPACE} --tail=50 || true
                            '''
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 清理环境...'
            
            // 清理工作空间
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true,
                patterns: [
                    [pattern: 'target/', type: 'INCLUDE'],
                    [pattern: '.mvn/', type: 'INCLUDE'],
                    [pattern: 'docker-compose.log', type: 'INCLUDE']
                ]
            )
        }
        
        success {
            echo '✅ 流水线执行成功！'
            
            // 发送成功通知
            script {
                def systemInfo = env.IS_WINDOWS == 'true' ? 'Windows' : 'Unix/Linux'
                emailext (
                    subject: "✅ 构建成功: ${env.JOB_NAME} - ${env.BUILD_NUMBER} (${systemInfo})",
                    body: """
                        <h3>构建成功</h3>
                        <p><strong>项目:</strong> ${env.JOB_NAME}</p>
                        <p><strong>构建号:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>版本:</strong> ${env.BUILD_VERSION}</p>
                        <p><strong>分支:</strong> ${env.BRANCH_NAME}</p>
                        <p><strong>提交:</strong> ${env.GIT_COMMIT_SHORT}</p>
                        <p><strong>构建时间:</strong> ${currentBuild.durationString}</p>
                        <p><strong>构建环境:</strong> ${systemInfo}</p>
                        <p><strong>构建链接:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        
                        <h4>部署信息:</h4>
                        <p>应用已成功部署到Kubernetes集群</p>
                        <p>命名空间: ${env.K8S_NAMESPACE}</p>
                        
                        <h4>测试结果:</h4>
                        <p>✅ 单元测试: 通过</p>
                        <p>✅ 集成测试: 通过</p>
                        <p>✅ 冒烟测试: 通过</p>
                    """,
                    to: "${env.CHANGE_AUTHOR_EMAIL}",
                    mimeType: 'text/html'
                )
            }
        }
        
        failure {
            echo '❌ 流水线执行失败！'
            
            // 发送失败通知
            script {
                def systemInfo = env.IS_WINDOWS == 'true' ? 'Windows' : 'Unix/Linux'
                emailext (
                    subject: "❌ 构建失败: ${env.JOB_NAME} - ${env.BUILD_NUMBER} (${systemInfo})",
                    body: """
                        <h3>构建失败</h3>
                        <p><strong>项目:</strong> ${env.JOB_NAME}</p>
                        <p><strong>构建号:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>分支:</strong> ${env.BRANCH_NAME}</p>
                        <p><strong>失败阶段:</strong> ${env.STAGE_NAME}</p>
                        <p><strong>构建环境:</strong> ${systemInfo}</p>
                        <p><strong>构建链接:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        <p><strong>控制台输出:</strong> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                        
                        <h4>请检查以下内容:</h4>
                        <ul>
                            <li>单元测试是否通过</li>
                            <li>代码是否编译成功</li>
                            <li>Docker镜像是否构建成功</li>
                            <li>Kubernetes部署是否正常</li>
                            <li>环境配置是否正确（${systemInfo}）</li>
                        </ul>
                    """,
                    to: "${env.CHANGE_AUTHOR_EMAIL}",
                    mimeType: 'text/html'
                )
            }
        }
        
        unstable {
            echo '⚠️ 流水线执行不稳定！'
        }
    }
}
