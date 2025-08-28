group "default" {
  targets = [
    "discovery-server",
    "config-server",
    "gateway",
    "monitor-service",
    "auth-service",
    "user-service",
  ]
}

target "_common" {
  platforms = ["linux/amd64"]
}

target "discovery-server" {
  inherits = ["_common"]
  context = "linghuzhiyan-discovery-server"
  dockerfile = "linghuzhiyan-discovery-server/Dockerfile"
  tags = ["discovery-server:j21"]
}

target "config-server" {
  inherits = ["_common"]
  context = "linghuzhiyan-config-server"
  dockerfile = "linghuzhiyan-config-server/Dockerfile"
  tags = ["config-server:j21"]
}

target "gateway" {
  inherits = ["_common"]
  context = "linghuzhiyan-gateway"
  dockerfile = "linghuzhiyan-gateway/Dockerfile"
  tags = ["gateway:j21"]
}

target "monitor-service" {
  inherits = ["_common"]
  context = "linghuzhiyan-monitor-service"
  dockerfile = "linghuzhiyan-monitor-service/Dockerfile"
  tags = ["monitor-service:j21"]
}

target "auth-service" {
  inherits = ["_common"]
  context = "linghuzhiyan-auth-service"
  dockerfile = "linghuzhiyan-auth-service/Dockerfile"
  tags = ["auth-service:j21"]
}

target "user-service" {
  inherits = ["_common"]
  context = "linghuzhiyan-user-service"
  dockerfile = "linghuzhiyan-user-service/Dockerfile"
  tags = ["user-service:j21"]
}
