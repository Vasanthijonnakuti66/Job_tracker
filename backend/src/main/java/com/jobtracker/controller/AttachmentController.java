package com.jobtracker.controller;

import com.jobtracker.dto.AttachmentResponse;
import com.jobtracker.entity.AttachmentType;
import com.jobtracker.security.UserPrincipal;
import com.jobtracker.service.AttachmentService;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/applications/{applicationId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                attachmentService.getAttachments(currentUser.getId(), applicationId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId,
            @RequestParam @NotNull AttachmentType type,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(
                attachmentService.upload(currentUser.getId(), applicationId, type, file));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId,
            @PathVariable Long attachmentId) {
        AttachmentService.AttachmentDownload download =
                attachmentService.getDownload(currentUser.getId(), applicationId, attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(download.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new FileSystemResource(download.path()));
    }
}
