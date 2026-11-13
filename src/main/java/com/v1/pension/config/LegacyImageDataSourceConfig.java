package com.v1.pension.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacyImageDataSourceConfig {
  @Bean
  @ConfigurationProperties("legacy.image.datasource")
  public DataSourceProperties legacyImageDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "legacyImageDataSource")
  public DataSource legacyImageDataSource() {
    return legacyImageDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
  }
}
