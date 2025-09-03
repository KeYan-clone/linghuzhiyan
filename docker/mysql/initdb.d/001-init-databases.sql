-- Initialize databases for services (MySQL 8 default collation)
CREATE DATABASE IF NOT EXISTS linghuzhiyan_auth
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS linghuzhiyan_user
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- Message service database
CREATE DATABASE IF NOT EXISTS linghuzhiyan_message
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- Resource service database
CREATE DATABASE IF NOT EXISTS linghuzhiyan_resource
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- Discussion service database
CREATE DATABASE IF NOT EXISTS linghuzhiyan_discussion
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- Experiment service database
CREATE DATABASE IF NOT EXISTS linghuzhiyan_experiment
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_0900_ai_ci;
