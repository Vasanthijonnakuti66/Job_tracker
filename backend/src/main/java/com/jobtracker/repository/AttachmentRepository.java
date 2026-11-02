package com.jobtracker.repository;

import com.jobtracker.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Attachment entities.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // All attachments for a specific application
    List<Attachment> findByApplicationId(Long applicationId);
}
