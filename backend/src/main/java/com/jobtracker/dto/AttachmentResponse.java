package com.jobtracker.dto;

import com.jobtracker.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        Long applicationId,
        String fileName,
        String fileType,
        Long fileSize,
        LocalDateTime uploadedAt
) {
    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getApplication().getId(),
                attachment.getFileName(),
                attachment.getFileType().name(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}
