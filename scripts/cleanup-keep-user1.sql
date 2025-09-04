-- Cleanup script: keep only user1 and their ROLE_ADMIN mapping; delete everything else
-- Safe to run multiple times.

-- Capture user1 id from user database
SELECT @user1_id := id FROM linghuzhiyan_user.users WHERE username = 'user1' LIMIT 1;

-- =========================
-- AUTH DATABASE (roles, user_roles, login_logs)
-- =========================
USE linghuzhiyan_auth;
SET FOREIGN_KEY_CHECKS = 0;

-- Keep only basic roles and ensure they exist
-- Role.id stores values like 'ROLE_ADMIN'
DELETE FROM roles WHERE id NOT IN ('ROLE_ADMIN','ROLE_TEACHER','ROLE_ASSISTANT','ROLE_STUDENT');

-- Upsert required roles (name 与 id 保持一致以简化)
INSERT INTO roles (id, name, created_at, updated_at) VALUES
	('ROLE_ADMIN','ROLE_ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), updated_at=NOW();

INSERT INTO roles (id, name, created_at, updated_at) VALUES
	('ROLE_TEACHER','ROLE_TEACHER', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), updated_at=NOW();

INSERT INTO roles (id, name, created_at, updated_at) VALUES
	('ROLE_ASSISTANT','ROLE_ASSISTANT', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), updated_at=NOW();

INSERT INTO roles (id, name, created_at, updated_at) VALUES
	('ROLE_STUDENT','ROLE_STUDENT', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), updated_at=NOW();

-- Keep only mapping (user1, ROLE_ADMIN) in user_roles
-- user_roles has columns user_id, role_id
DELETE FROM user_roles 
WHERE NOT (user_id = @user1_id AND role_id = 'ROLE_ADMIN');

-- Clear login logs (not identity info) if the table exists
SET @has_login := 0;
SELECT COUNT(*) INTO @has_login FROM information_schema.tables 
WHERE table_schema = 'linghuzhiyan_auth' AND table_name = 'login_logs';
SET @sql := IF(@has_login > 0, 'TRUNCATE TABLE login_logs', NULL);
SET @sql := IFNULL(@sql, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- USER DATABASE (users)
-- =========================
USE linghuzhiyan_user;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM users WHERE username <> 'user1';
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- EXPERIMENT DATABASE (questions, experiments, tasks, assignments, submissions, evaluations)
-- =========================
USE linghuzhiyan_experiment;
SET FOREIGN_KEY_CHECKS = 0;
-- Delete children first in case of FKs
DELETE FROM experiment_evaluation;
DELETE FROM experiment_submission;
DELETE FROM experiment_assignment;
DELETE FROM experiment_task;
DELETE FROM question;
DELETE FROM experiment;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- RESOURCE DATABASE (resource)
-- =========================
USE linghuzhiyan_resource;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM resource;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- MESSAGE DATABASE (truncate all tables dynamically)
-- =========================
-- Will no-op if DB or tables absent
DROP PROCEDURE IF EXISTS truncate_schema;
DELIMITER $$
CREATE PROCEDURE truncate_schema(IN p_schema VARCHAR(64))
BEGIN
	DECLARE done INT DEFAULT 0;
	DECLARE t VARCHAR(128);
	DECLARE cur CURSOR FOR 
		SELECT table_name FROM information_schema.tables WHERE table_schema = p_schema;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

		SET @fk0 := 'SET FOREIGN_KEY_CHECKS = 0';
	PREPARE s FROM @fk0; EXECUTE s; DEALLOCATE PREPARE s;

	OPEN cur;
	read_loop: LOOP
		FETCH cur INTO t;
		IF done = 1 THEN LEAVE read_loop; END IF;
		-- Use DELETE to avoid PREPARE not supporting TRUNCATE in some MySQL versions
		SET @stmt := CONCAT('DELETE FROM `', p_schema, '`.`', t, '`');
		PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;
	END LOOP;
	CLOSE cur;

	SET @fk1 := 'SET FOREIGN_KEY_CHECKS = 1';
	PREPARE s FROM @fk1; EXECUTE s; DEALLOCATE PREPARE s;
END$$
DELIMITER ;

CALL truncate_schema('linghuzhiyan_message');
DROP PROCEDURE IF EXISTS truncate_schema;

-- Done
