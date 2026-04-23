package com.gdut.secondhand.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RdsDataSourceConfig {

    private final HuaweiCloudConfig huaweiCloudConfig;

    @Bean
    @Primary
    public DataSource dataSource() {
        HuaweiCloudConfig.RdsConfig rds = huaweiCloudConfig.getRds();

        if (!Boolean.TRUE.equals(rds.getEnabled())) {
            log.warn("RDS is disabled, returning default datasource configuration");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(rds.buildJdbcUrl());
        hikariConfig.setUsername(rds.getUsername());
        hikariConfig.setPassword(rds.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        HuaweiCloudConfig.RdsConfig.PoolConfig pool = rds.getPool();
        hikariConfig.setMaximumPoolSize(pool.getMaxActive());
        hikariConfig.setMinimumIdle(pool.getMinIdle());
        hikariConfig.setIdleTimeout(pool.getMaxWait());
        hikariConfig.setConnectionTimeout(pool.getMaxWait());
        hikariConfig.setPoolName("HikariCP-RDS");

        log.info("RDS DataSource configured: host={}, port={}, database={}",
            rds.getHost(), rds.getPort(), rds.getDatabase());

        return new HikariDataSource(hikariConfig);
    }
}