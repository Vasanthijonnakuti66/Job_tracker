package com.jobtracker.repository;

import com.jobtracker.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Attachment entities.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // All attachments for a specific application
    List<Attachment> findByApplicationIdOrderByUploadedAtDesc(Long applicationId);

    Optional<Attachment> findByIdAndApplicationId(Long id, Long applicationId);

    @Query("""
            SELECT a FROM Attachment a
            WHERE a.id = :attachmentId
              AND a.application.id = :applicationId
              AND a.application.user.id = :userId
            """)
    Optional<Attachment> findOwnedAttachment(
            @Param("attachmentId") Long attachmentId,
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId
    );
}
