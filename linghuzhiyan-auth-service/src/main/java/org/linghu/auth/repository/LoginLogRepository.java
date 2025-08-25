package org.linghu.auth.repository;

import org.linghu.auth.domain.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志Repository接口
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    /**
     * 根据用户ID查找登录日志
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(String userId);

    /**
     * 根据IP地址查找登录日志
     * @param ipAddress IP地址
     * @return 登录日志列表
     */
    List<LoginLog> findByIpAddressOrderByLoginTimeDesc(String ipAddress);

    /**
     * 查找指定时间范围内的登录日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志列表
     */
    @Query("SELECT l FROM LoginLog l WHERE l.loginTime BETWEEN :startTime AND :endTime ORDER BY l.loginTime DESC")
    List<LoginLog> findByLoginTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内成功登录次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功登录次数
     */
    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.status = 'SUCCESS' AND l.loginTime BETWEEN :startTime AND :endTime")
    Long countSuccessfulLoginsBetween(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内失败登录次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败登录次数
     */
    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.status = 'FAILED' AND l.loginTime BETWEEN :startTime AND :endTime")
    Long countFailedLoginsBetween(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);
}
