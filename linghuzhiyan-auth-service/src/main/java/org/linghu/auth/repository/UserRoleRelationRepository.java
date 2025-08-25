package org.linghu.auth.repository;

import org.linghu.auth.domain.UserRoleRelation;
import org.linghu.auth.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 用户角色关系Repository接口
 */
@Repository
public interface UserRoleRelationRepository extends JpaRepository<UserRoleRelation, UserRoleId> {

    /**
     * 根据用户ID查找用户角色关系
     * @param userId 用户ID
     * @return 用户角色关系列表
     */
    @Query("SELECT ur FROM UserRoleRelation ur WHERE ur.id.userId = :userId")
    List<UserRoleRelation> findByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID查找用户角色关系
     * @param roleId 角色ID
     * @return 用户角色关系列表
     */
    @Query("SELECT ur FROM UserRoleRelation ur WHERE ur.id.roleId = :roleId")
    List<UserRoleRelation> findByRoleId(@Param("roleId") String roleId);

    /**
     * 根据用户ID获取角色ID集合
     * @param userId 用户ID
     * @return 角色ID集合
     */
    @Query("SELECT ur.id.roleId FROM UserRoleRelation ur WHERE ur.id.userId = :userId")
    Set<String> findRoleIdsByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID删除用户角色关系
     * @param userId 用户ID
     */
    @Query("DELETE FROM UserRoleRelation ur WHERE ur.id.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID删除用户角色关系
     * @param roleId 角色ID
     */
    @Query("DELETE FROM UserRoleRelation ur WHERE ur.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 检查用户是否具有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在
     */
    boolean existsByIdUserIdAndIdRoleId(String userId, String roleId);
}
