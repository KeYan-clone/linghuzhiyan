package org.linghu.message.repository;

import org.linghu.message.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    // 只保留findAll等通用方法
}
