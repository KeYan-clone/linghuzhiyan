package org.linghu.message.service.impl;

import org.linghu.message.domain.Announcement;
import org.linghu.message.dto.AnnouncementDTO;
import org.linghu.message.repository.AnnouncementRepository;
import org.linghu.message.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    @Autowired
    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Override
    public AnnouncementDTO createAnnouncement(AnnouncementDTO dto) {
        Announcement announcement = Announcement.builder()
                .id(dto.getId() == null ? java.util.UUID.randomUUID().toString() : dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        announcementRepository.save(announcement);
        return toDTO(announcement);
    }

    @Override
    public AnnouncementDTO getAnnouncementById(String id) {
        return announcementRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public List<AnnouncementDTO> getAllAnnouncements() {
        List<Announcement> list = announcementRepository.findAll();
        List<AnnouncementDTO> result = new ArrayList<>();
        for (Announcement a : list) {
            result.add(toDTO(a));
        }
        return result;
    }

    @Override
    public void deleteAnnouncement(String id) {
        announcementRepository.deleteById(id);
    }

    @Override
    public AnnouncementDTO updateAnnouncement(String id, AnnouncementDTO dto) {
        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement == null) {
            return null;
        }
        if (dto.getTitle() != null) announcement.setTitle(dto.getTitle());
        if (dto.getContent() != null) announcement.setContent(dto.getContent());
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);
        return toDTO(announcement);
    }

    private AnnouncementDTO toDTO(Announcement a) {
        return AnnouncementDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .createdAt(a.getCreatedAt() == null ? null : a.getCreatedAt().toString())
                .updatedAt(a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString())
                .build();
    }
}
