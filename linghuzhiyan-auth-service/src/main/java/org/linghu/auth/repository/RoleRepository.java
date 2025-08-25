package org.linghu.auth.repository;

import org.linghu.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色Repository接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * 根据角色名查找角色
     * @param name 角色名
     * @return 角色
     */
    Optional<Role> findByName(String name);

    /**
     * 检查角色名是否存在
     * @param name 角色名
     * @return 是否存在
     */
    boolean existsByName(String name);
}
