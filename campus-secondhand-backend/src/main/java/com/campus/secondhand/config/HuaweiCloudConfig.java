package com.campus.secondhand.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource(value = "classpath:huawei-cloud.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "huawei-cloud")
public class HuaweiCloudConfig {

    private RdsConfig rds = new RdsConfig();
    private ObsConfigProperties obs = new ObsConfigProperties();

    @Data
    public static class RdsConfig {
        private Boolean enabled = true;
        private String host = "localhost";
        private Integer port = 3306;
        private String database = "campus_secondhand";
        private String username = "root";
        private String password = "root";
        private String charset = "UTF-8";
        private String timezone = "Asia/Shanghai";
        private Boolean useSsl = false;
        private PoolConfig pool = new PoolConfig();

        @Data
        public static class PoolConfig {
            private Integer maxActive = 20;
            private Integer maxIdle = 10;
            private Integer minIdle = 5;
            private Long maxWait = 30000L;
        }

        public String buildJdbcUrl() {
            return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=%s&useSSL=%s&serverTimezone=%s",
                host, port, database, charset, useSsl.toString(), timezone
            );
        }
    }

    @Data
    public static class ObsConfigProperties {
        private Boolean enabled = false;
        private String accessKey;
        private String secretKey;
        private String endpoint = "https://obs.cn-north-4.myhuaweicloud.com";
        private String region = "cn-north-4";
        private String bucket;
        private UploadConfig upload = new UploadConfig();

        @Data
        public static class UploadConfig {
            private Long maxSize = 10485760L;
            private String allowedTypes = "jpg,jpeg,png,gif,webp";
            private String pathPrefix = "products/";
        }
    }
}