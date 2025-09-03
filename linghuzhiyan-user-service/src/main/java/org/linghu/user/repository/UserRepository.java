package org.linghu.user.repository;

import org.linghu.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口，负责用户数据的持久化
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户可选项
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户可选项
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByEmail(String email);
    
    /**
     * 查询未删除的用户（分页）
     * 
     * @param pageable 分页参数
     * @return 未删除用户的分页结果
     */
    Page<User> findByIsDeletedFalse(Pageable pageable);

    /**
     * 查询未删除的用户（不分页）
     *
     * @return 未删除用户列表
     */
    List<User> findByIsDeletedFalse();
}
