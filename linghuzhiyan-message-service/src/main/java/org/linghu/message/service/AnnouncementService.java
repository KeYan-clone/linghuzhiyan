package org.linghu.message.service;

import org.linghu.message.dto.AnnouncementDTO;

import java.util.List;

public interface AnnouncementService {
    AnnouncementDTO createAnnouncement(AnnouncementDTO announcementDTO);
    AnnouncementDTO getAnnouncementById(String id);
    List<AnnouncementDTO> getAllAnnouncements();
    void deleteAnnouncement(String id);
    AnnouncementDTO updateAnnouncement(String id, AnnouncementDTO announcementDTO);
}
