package com.tableorder.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(fileStorageService, "region", "ap-northeast-2");
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_success() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[1024]);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = fileStorageService.uploadImage(1L, file);

        assertThat(result).startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/menus/1/");
        assertThat(result).endsWith(".jpg");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("이미지 업로드 - 파일 크기 초과 시 예외")
    void uploadImage_fileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", largeContent);

        assertThatThrownBy(() -> fileStorageService.uploadImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    @DisplayName("이미지 업로드 - 허용되지 않는 파일 형식 시 예외")
    void uploadImage_invalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.pdf", "application/pdf", new byte[1024]);

        assertThatThrownBy(() -> fileStorageService.uploadImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 파일 형식");
    }

    @Test
    @DisplayName("이미지 업로드 - 빈 파일 시 예외")
    void uploadImage_emptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.uploadImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비어있습니다");
    }

    @Test
    @DisplayName("이미지 삭제 성공")
    void deleteImage_success() {
        String imageUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/menus/1/uuid.jpg";

        fileStorageService.deleteImage(imageUrl);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().key()).isEqualTo("menus/1/uuid.jpg");
    }

    @Test
    @DisplayName("이미지 삭제 - null URL이면 아무 동작 안 함")
    void deleteImage_nullUrl() {
        fileStorageService.deleteImage(null);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 삭제 - S3 실패해도 예외 발생하지 않음 (best-effort)")
    void deleteImage_s3Failure_noException() {
        String imageUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/menus/1/uuid.jpg";

        doThrow(S3Exception.builder().message("S3 error").build())
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // 예외가 발생하지 않아야 함
        fileStorageService.deleteImage(imageUrl);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("PNG 이미지 업로드 성공")
    void uploadImage_png() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", new byte[1024]);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = fileStorageService.uploadImage(1L, file);

        assertThat(result).endsWith(".png");
    }

    @Test
    @DisplayName("WebP 이미지 업로드 성공")
    void uploadImage_webp() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.webp", "image/webp", new byte[1024]);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = fileStorageService.uploadImage(1L, file);

        assertThat(result).endsWith(".webp");
    }
}
