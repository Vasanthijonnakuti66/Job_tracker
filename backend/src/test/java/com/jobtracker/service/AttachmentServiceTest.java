package com.jobtracker.service;

import com.jobtracker.entity.Application;
import com.jobtracker.entity.Attachment;
import com.jobtracker.entity.AttachmentType;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.AttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    private static final byte[] PDF_CONTENT = "%PDF-1.7\nexample".getBytes();

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AttachmentRepository attachmentRepository;

    @TempDir
    Path storageDirectory;

    private AttachmentService service;

    @BeforeEach
    void setUp() {
        service = new AttachmentService(
                applicationRepository, attachmentRepository, storageDirectory.toString(), 1024);
    }

    @Test
    void uploadStoresPdfWithGeneratedNameAndReturnsMetadata() throws Exception {
        Application application = new Application();
        application.setId(21L);
        when(applicationRepository.findByIdAndUserId(21L, 7L))
                .thenReturn(Optional.of(application));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment saved = invocation.getArgument(0);
            saved.setId(34L);
            return saved;
        });

        TransactionSynchronizationManager.initSynchronization();
        try {
            var response = service.upload(7L, 21L, AttachmentType.RESUME,
                    new MockMultipartFile("file", "my-resume.pdf", "application/pdf", PDF_CONTENT));

            assertEquals(34L, response.id());
            assertEquals("my-resume.pdf", response.fileName());
            assertEquals("RESUME", response.fileType());
            ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
            verify(attachmentRepository).save(attachmentCaptor.capture());
            Attachment saved = attachmentCaptor.getValue();
            assertEquals("RESUME", saved.getFileType().name());
            assertEquals(PDF_CONTENT.length, saved.getFileSize());
            assertEquals(Path.of(saved.getFilePath()).getFileName().toString(), saved.getFilePath());
            assertEquals(PDF_CONTENT.length, Files.size(storageDirectory.resolve(saved.getFilePath())));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void uploadRejectsNonPdfContentBeforeWritingMetadata() {
        when(applicationRepository.findByIdAndUserId(21L, 7L))
                .thenReturn(Optional.of(new Application()));

        assertThrows(IllegalArgumentException.class, () -> service.upload(
                7L, 21L, AttachmentType.JOB_DESCRIPTION,
                new MockMultipartFile("file", "notes.pdf", "application/pdf", "not a pdf".getBytes())));

        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void uploadRejectsApplicationOwnedByAnotherUser() {
        when(applicationRepository.findByIdAndUserId(21L, 7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.upload(
                7L, 21L, AttachmentType.RESUME,
                new MockMultipartFile("file", "resume.pdf", "application/pdf", PDF_CONTENT)));

        verify(attachmentRepository, never()).save(any(Attachment.class));
    }
}
