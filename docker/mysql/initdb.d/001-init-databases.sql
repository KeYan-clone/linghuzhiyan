-- Initialize databases for services (MySQL 8 default collation)
CREATE DATABASE IF NOT EXISTS linghuzhiyan_auth
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS linghuzhiyan_user
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;
