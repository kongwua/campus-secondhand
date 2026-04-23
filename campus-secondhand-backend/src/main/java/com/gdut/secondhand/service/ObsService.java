package com.gdut.secondhand.service;

import com.gdut.secondhand.config.ObsConfig;
import com.gdut.secondhand.exception.BusinessException;
import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObsService {

    private final ObsConfig obsConfig;
    private ObsClient obsClient;

    @PostConstruct
    public void init() {
        if (Boolean.TRUE.equals(obsConfig.getEnabled())) {
            try {
                obsClient = new ObsClient(
                    obsConfig.getAccessKey(),
                    obsConfig.getSecretKey(),
                    obsConfig.getEndpoint()
                );
                log.info("OBS client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize OBS client", e);
            }
        } else {
            log.info("OBS is disabled, using mock mode");
        }
    }

    @PreDestroy
    public void destroy() {
        if (obsClient != null) {
            try {
                obsClient.close();
            } catch (Exception e) {
                log.error("Failed to close OBS client", e);
            }
        }
    }

    /**
     * Generate a presigned URL for uploading an object to OBS
     * @param objectKey the object key (file path in bucket)
     * @return presigned put URL
     */
    public String generatePresignedPutUrl(String objectKey) {
        if (!Boolean.TRUE.equals(obsConfig.getEnabled()) || obsClient == null) {
            // Mock mode: return a fake URL for development
            return generateMockUrl(objectKey, "PUT");
        }

        try {
            TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.PUT,
                3600 // 1 hour expiration
            );
            request.setBucketName(obsConfig.getBucket());
            request.setObjectKey(objectKey);
            
            TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
            return response.getSignedUrl();
        } catch (Exception e) {
            log.error("Failed to generate presigned PUT URL for {}", objectKey, e);
            throw BusinessException.of("生成上传URL失败");
        }
    }

    /**
     * Generate a presigned URL for downloading an object from OBS
     * @param objectKey the object key (file path in bucket)
     * @return presigned get URL
     */
    public String generatePresignedGetUrl(String objectKey) {
        if (!Boolean.TRUE.equals(obsConfig.getEnabled()) || obsClient == null) {
            // Mock mode: return a fake URL for development
            return generateMockUrl(objectKey, "GET");
        }

        try {
            TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.GET,
                3600 // 1 hour expiration
            );
            request.setBucketName(obsConfig.getBucket());
            request.setObjectKey(objectKey);
            
            TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
            return response.getSignedUrl();
        } catch (Exception e) {
            log.error("Failed to generate presigned GET URL for {}", objectKey, e);
            throw BusinessException.of("生成下载URL失败");
        }
    }

    /**
     * Generate a unique object key for image upload
     * @param originalFilename the original filename
     * @return unique object key
     */
    public String generateObjectKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "products/" + UUID.randomUUID().toString() + extension;
    }

    private String generateMockUrl(String objectKey, String method) {
        return String.format(
            "https://%s.%s/%s?mock=true&method=%s&expires=%d",
            obsConfig.getBucket(),
            obsConfig.getEndpoint(),
            objectKey,
            method,
            System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        );
    }
}