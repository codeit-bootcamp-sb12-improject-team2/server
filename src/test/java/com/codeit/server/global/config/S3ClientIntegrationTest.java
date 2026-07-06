package com.codeit.server.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class S3ClientIntegrationTest {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Test
    void testS3ConnectionAndUpload() {
        // 1. S3Client가 스프링 빈으로 정상 주입되었는지 확인
        assertThat(s3Client).isNotNull();
        System.out.println("====== S3Client is successfully injected! ======");

        // 2. 실제 S3 버킷으로 테스트 파일 업로드 시도하여 업로드 기능 및 자격 증명 확인
        String testKey = "test/s3-injection-check.txt";
        String testContent = "This is a test file for verification of S3 injection and credentials at " + java.time.LocalDateTime.now();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(testKey)
                    .contentType("text/plain")
                    .build();

            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromString(testContent));

            // 업로드 응답 결과 확인
            assertThat(response).isNotNull();
            assertThat(response.sdkHttpResponse().isSuccessful()).isTrue();
            System.out.println("====== S3 Upload Succeeded! Target Bucket: " + bucket + ", Key: " + testKey + " ======");
        } catch (Exception e) {
            System.err.println("====== S3 Upload Failed! Check your AWS credentials and bucket permissions. ======");
            e.printStackTrace();
            throw e;
        }
    }
}
