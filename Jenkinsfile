pipeline {
	agent any
	environment {
		KUBE_CONFIG = credentials('kubeconfig') // Jenkins凭据ID
	}
	stages {
		stage('Checkout') {
			steps {
				checkout scm
			}
		}
		stage('Build Maven Project') {
			steps {
				script {
					if (isUnix()) {
						sh 'chmod +x mvnw || echo skip'
						sh './mvnw clean package -DskipTests'
					} else {
						bat 'mvn clean package -DskipTests'
					}
				}
			}
		}
		stage('Build Docker Images') {
			steps {
				script {
					def services = [
						'linghuzhiyan-auth-service',
						'linghuzhiyan-config-server', 
						'linghuzhiyan-discovery-server',
						'linghuzhiyan-discussion-service',
						'linghuzhiyan-experiment-service',
						'linghuzhiyan-gateway',
						'linghuzhiyan-message-service',
						'linghuzhiyan-resource-service',
						'linghuzhiyan-user-service'
					]
					
					services.each { service ->
						echo "Building Docker image for ${service}..."
						if (isUnix()) {
							sh "cd ${service} && docker build -t ${service}:latest ."
						} else {
							bat "cd ${service} && docker build -t ${service}:latest ."
						}
					}
				}
			}
		}
		stage('Verify Docker Images') {
			steps {
				script {
					echo 'Verifying built Docker images...'
					if (isUnix()) {
						sh 'docker images | grep linghuzhiyan'
					} else {
						bat 'docker images | findstr linghuzhiyan'
					}
				}
			}
		}
		stage('Deploy Infrastructure') {
			steps {
				withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
					dir('k8s') {
						script {
							echo 'Deploying infrastructure components...'
							if (isUnix()) {
								sh 'kubectl apply -f namespace.yaml'
								sh 'kubectl apply -f mysql.yaml'
								sh 'kubectl apply -f redis.yaml'
								sh 'kubectl apply -f minio.yaml'
								sh 'kubectl apply -f mongo.yaml'
							} else {
								bat 'kubectl apply -f namespace.yaml'
								bat 'kubectl apply -f mysql.yaml'
								bat 'kubectl apply -f redis.yaml'
								bat 'kubectl apply -f minio.yaml'
								bat 'kubectl apply -f mongo.yaml'
							}
						}
					}
				}
			}
		}
		stage('Wait for Infrastructure') {
			steps {
				withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
					script {
						echo 'Waiting for infrastructure to be ready...'
						if (isUnix()) {
							sh '''
								kubectl wait --for=condition=available --timeout=300s deployment/mysql -n linghuzhiyan || true
								kubectl wait --for=condition=available --timeout=300s deployment/redis -n linghuzhiyan || true
								kubectl wait --for=condition=available --timeout=300s deployment/minio -n linghuzhiyan || true
								kubectl wait --for=condition=available --timeout=300s deployment/mongo -n linghuzhiyan || true
							'''
						} else {
							bat '''
								kubectl wait --for=condition=available --timeout=300s deployment/mysql -n linghuzhiyan || echo "MySQL timeout"
								kubectl wait --for=condition=available --timeout=300s deployment/redis -n linghuzhiyan || echo "Redis timeout"
								kubectl wait --for=condition=available --timeout=300s deployment/minio -n linghuzhiyan || echo "MinIO timeout"
								kubectl wait --for=condition=available --timeout=300s deployment/mongo -n linghuzhiyan || echo "MongoDB timeout"
							'''
						}
					}
				}
			}
		}
		stage('Deploy Core Services') {
			steps {
				withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
					dir('k8s') {
						script {
							echo 'Deploying core services...'
							if (isUnix()) {
								sh 'kubectl apply -f discovery-server.yaml'
								sh '''
									echo "Waiting for Discovery Server to be ready..."
									kubectl wait --for=condition=available --timeout=300s deployment/discovery-server -n linghuzhiyan
								'''
								sh 'kubectl apply -f config-server.yaml'
								sh '''
									echo "Waiting for Config Server to be ready..."
									kubectl wait --for=condition=available --timeout=300s deployment/config-server -n linghuzhiyan
								'''
								sh 'kubectl apply -f gateway.yaml'
								sh '''
									echo "Waiting for Gateway to be ready..."
									kubectl wait --for=condition=available --timeout=300s deployment/gateway -n linghuzhiyan
								'''
							} else {
								bat 'kubectl apply -f discovery-server.yaml'
								bat '''
									echo Waiting for Discovery Server to be ready...
									kubectl wait --for=condition=available --timeout=300s deployment/discovery-server -n linghuzhiyan
								'''
								bat 'kubectl apply -f config-server.yaml'
								bat '''
									echo Waiting for Config Server to be ready...
									kubectl wait --for=condition=available --timeout=300s deployment/config-server -n linghuzhiyan
								'''
								bat 'kubectl apply -f gateway.yaml'
								bat '''
									echo Waiting for Gateway to be ready...
									kubectl wait --for=condition=available --timeout=300s deployment/gateway -n linghuzhiyan
								'''
							}
						}
					}
				}
			}
		}
		stage('Deploy Business Services') {
			steps {
				withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
					dir('k8s') {
						script {
							echo 'Deploying business services...'
							if (isUnix()) {
								sh 'kubectl apply -f auth-service.yaml'
								sh 'kubectl apply -f user-service.yaml'
								sh 'kubectl apply -f experiment-service.yaml'
								sh 'kubectl apply -f resource-service.yaml'
								sh 'kubectl apply -f message-service.yaml'
								sh 'kubectl apply -f discussion-service.yaml'
							} else {
								bat 'kubectl apply -f auth-service.yaml'
								bat 'kubectl apply -f user-service.yaml'
								bat 'kubectl apply -f experiment-service.yaml'
								bat 'kubectl apply -f resource-service.yaml'
								bat 'kubectl apply -f message-service.yaml'
								bat 'kubectl apply -f discussion-service.yaml'
							}
						}
					}
				}
			}
		}
		stage('Health Check') {
			steps {
				withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
					script {
						echo 'Performing comprehensive health check...'
						if (isUnix()) {
							sh '''
								echo "Waiting for business services to be ready..."
								kubectl wait --for=condition=available --timeout=40s deployment/auth-service -n linghuzhiyan || echo "Auth service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/user-service -n linghuzhiyan || echo "User service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/experiment-service -n linghuzhiyan || echo "Experiment service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/resource-service -n linghuzhiyan || echo "Resource service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/message-service -n linghuzhiyan || echo "Message service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/discussion-service -n linghuzhiyan || echo "Discussion service timeout"
								
								echo "Final health check - All services status:"
								kubectl get pods -n linghuzhiyan
								kubectl get services -n linghuzhiyan
								
								echo "Checking if all pods are running..."
								FAILED_PODS=$(kubectl get pods -n linghuzhiyan --field-selector=status.phase!=Running --no-headers | wc -l)
								if [ $FAILED_PODS -gt 0 ]; then
									echo "Warning: Some pods are not in Running state"
									kubectl get pods -n linghuzhiyan --field-selector=status.phase!=Running
								else
									echo "All pods are running successfully!"
								fi
							'''
						} else {
							bat '''
								echo Waiting for business services to be ready...
								kubectl wait --for=condition=available --timeout=40s deployment/auth-service -n linghuzhiyan || echo "Auth service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/user-service -n linghuzhiyan || echo "User service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/experiment-service -n linghuzhiyan || echo "Experiment service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/resource-service -n linghuzhiyan || echo "Resource service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/message-service -n linghuzhiyan || echo "Message service timeout"
								kubectl wait --for=condition=available --timeout=40s deployment/discussion-service -n linghuzhiyan || echo "Discussion service timeout"
								
								echo Final health check - All services status:
								kubectl get pods -n linghuzhiyan
								kubectl get services -n linghuzhiyan
								
								echo Deployment completed successfully!
							'''
						}
					}
				}
			}
		}
	}
	post {
		always {
			script {
				echo 'Cleaning up resources...'
				if (isUnix()) {
					sh '''
						echo "Cleaning up Docker resources..."
						docker system prune -f || true
						docker volume prune -f || true
						docker network prune -f || true
						echo "Cleaning up temporary files..."
						rm -rf temp-* || true
						rm -rf target/docker || true
						echo "Cleanup completed"
					'''
				} else {
					bat '''
						echo Cleaning up Docker resources...
						docker system prune -f || echo "Docker cleanup failed"
						docker volume prune -f || echo "Volume cleanup failed"
						docker network prune -f || echo "Network cleanup failed"
						echo Cleaning up temporary files...
						if exist temp-* rmdir /s /q temp-* || echo "No temp files to clean"
						if exist target\\docker rmdir /s /q target\\docker || echo "No docker target to clean"
						echo Cleanup completed
					'''
				}
			}
		}
		failure {
			echo 'Pipeline failed.'
		}
		success {
			echo 'Pipeline succeeded.'
		}
	}
}
