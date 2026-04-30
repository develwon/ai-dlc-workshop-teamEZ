package com.tableorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    public String uploadImage(Long storeId, MultipartFile file) {
        validateImageFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String key = String.format("menus/%d/%s.%s", storeId, UUID.randomUUID(), extension);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            log.info("이미지 업로드 완료 [storeId={}] [key={}] [size={}bytes]", storeId, key, file.getSize());
            return imageUrl;
        } catch (IOException e) {
            log.error("이미지 업로드 실패 [storeId={}] [key={}]", storeId, key, e);
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("이미지 삭제 완료 [key={}]", key);
        } catch (Exception e) {
            log.warn("이미지 삭제 실패 [imageUrl={}] [error={}]", imageUrl, e.getMessage());
            // best-effort: 삭제 실패해도 비즈니스 로직 중단하지 않음
        }
    }

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다 (JPEG, PNG, GIF, WebP만 허용).");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String extractKeyFromUrl(String imageUrl) {
        // URL 형식: https://{bucket}.s3.{region}.amazonaws.com/{key}
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        // 대체 URL 형식 처리
        int index = imageUrl.indexOf("amazonaws.com/");
        if (index >= 0) {
            return imageUrl.substring(index + "amazonaws.com/".length());
        }
        return imageUrl;
    }
}
