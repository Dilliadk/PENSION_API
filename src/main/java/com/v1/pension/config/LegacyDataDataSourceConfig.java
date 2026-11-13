package com.v1.pension.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacyDataDataSourceConfig {
  @Bean
  @ConfigurationProperties("legacy.data.datasource")
  public DataSourceProperties legacyDataDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "legacyDataDataSource")
  public DataSource legacyDataSource() {
    return legacyDataDataSourceProperties()
        .initializeDataSourceBuilder()
        .build();
  }
}
