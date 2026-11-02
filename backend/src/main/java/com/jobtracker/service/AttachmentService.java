package com.jobtracker.service;

import com.jobtracker.dto.AttachmentResponse;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.Attachment;
import com.jobtracker.entity.AttachmentType;
import com.jobtracker.exception.FileStorageException;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.AttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};

    private final ApplicationRepository applicationRepository;
    private final AttachmentRepository attachmentRepository;
    private final Path storageDirectory;
    private final long maxFileSizeBytes;

    public AttachmentService(ApplicationRepository applicationRepository,
                             AttachmentRepository attachmentRepository,
                             @Value("${app.attachments.storage-path:uploads}") String storagePath,
                             @Value("${app.attachments.max-size-bytes:10485760}") long maxFileSizeBytes) {
        this.applicationRepository = applicationRepository;
        this.attachmentRepository = attachmentRepository;
        this.storageDirectory = Path.of(storagePath).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<AttachmentResponse> getAttachments(Long userId, Long applicationId) {
        findUserApplication(userId, applicationId);
        return attachmentRepository.findByApplicationIdOrderByUploadedAtDesc(applicationId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Transactional
    public AttachmentResponse upload(Long userId, Long applicationId, AttachmentType type,
                                     MultipartFile file) {
        Application application = findUserApplication(userId, applicationId);
        String originalFileName = validate(file);
        String storedFileName = UUID.randomUUID() + ".pdf";
        Path target = resolveStoredFile(storedFileName);

        try {
            Files.createDirectories(storageDirectory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException cleanupError) {
                ex.addSuppressed(cleanupError);
                log.error("Could not remove incomplete upload {}", target, cleanupError);
            }
            throw new FileStorageException("Could not save the uploaded PDF", ex);
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        Files.deleteIfExists(target);
                    } catch (IOException ex) {
                        log.error("Could not remove upload after database rollback: {}", target, ex);
                    }
                }
            }
        });

        Attachment attachment = new Attachment();
        attachment.setApplication(application);
        attachment.setFileName(originalFileName);
        attachment.setFilePath(storedFileName);
        attachment.setFileType(type);
        attachment.setFileSize(file.getSize());

        try {
            return AttachmentResponse.from(attachmentRepository.save(attachment));
        } catch (RuntimeException ex) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException cleanupError) {
                ex.addSuppressed(cleanupError);
                log.error("Could not remove orphaned upload {}", target, cleanupError);
            }
            throw ex;
        }
    }

    public AttachmentDownload getDownload(Long userId, Long applicationId, Long attachmentId) {
        findUserApplication(userId, applicationId);
        Attachment attachment = attachmentRepository
                .findOwnedAttachment(attachmentId, applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment", "id", attachmentId));
        Path file = resolveStoredFile(attachment.getFilePath());
        if (!Files.isRegularFile(file)) {
            throw new FileStorageException(
                    "The stored attachment file is missing", new IOException("Missing " + file));
        }
        return new AttachmentDownload(attachment.getFileName(), file, attachment.getFileSize());
    }

    public void deleteFilesAfterApplicationDeleted(List<Attachment> attachments) {
        List<Path> files = attachments.stream()
                .map(attachment -> resolveStoredFile(attachment.getFilePath()))
                .toList();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Path file : files) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException ex) {
                        log.error("Could not remove attachment file after application deletion: {}",
                                file, ex);
                    }
                }
            }
        });
    }

    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Select a PDF file to upload");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException(
                    "PDF files must be " + maxFileSizeBytes + " bytes or smaller");
        }

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("The uploaded file must have a name");
        }
        String baseName = original.replace('\\', '/');
        baseName = baseName.substring(baseName.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();
        if (baseName.isBlank() || baseName.length() > 255
                || !baseName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files with a valid file name are allowed");
        }

        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(PDF_SIGNATURE.length);
            if (!java.util.Arrays.equals(signature, PDF_SIGNATURE)) {
                throw new IllegalArgumentException("The uploaded file is not a valid PDF");
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not read the uploaded PDF", ex);
        }
        return baseName;
    }

    private Application findUserApplication(Long userId, Long applicationId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application", "id", applicationId));
    }

    private Path resolveStoredFile(String fileName) {
        Path file = storageDirectory.resolve(fileName).normalize();
        if (!file.startsWith(storageDirectory)) {
            throw new FileStorageException(
                    "Attachment path is invalid", new IOException("Path escaped storage directory"));
        }
        return file;
    }

    public record AttachmentDownload(String fileName, Path path, Long size) {
    }
}
