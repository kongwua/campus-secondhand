package com.gdut.secondhand.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
public class ObsConfig {

    private final HuaweiCloudConfig huaweiCloudConfig;

    public String getAccessKey() {
        return huaweiCloudConfig.getObs().getAccessKey();
    }

    public String getSecretKey() {
        return huaweiCloudConfig.getObs().getSecretKey();
    }

    public String getEndpoint() {
        return huaweiCloudConfig.getObs().getEndpoint();
    }

    public String getBucket() {
        return huaweiCloudConfig.getObs().getBucket();
    }

    public Boolean getEnabled() {
        return huaweiCloudConfig.getObs().getEnabled();
    }

    public HuaweiCloudConfig.ObsConfigProperties getObsProperties() {
        return huaweiCloudConfig.getObs();
    }
}