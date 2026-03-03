package com.sakata.boilerplate.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Datasource PRIMARY → MySQL
 * Chứa: users, roles, permissions, user_roles, role_permissions
 *
 * Scan mapper: com.sakata.boilerplate.mapper.primary
 * XML mapper: classpath:mapper/primary/*.xml
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.sakata.boilerplate.mapper.primary", sqlSessionFactoryRef = "primarySqlSessionFactory")
@Slf4j
public class PrimaryDataSourceConfig {

    @Bean("primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "datasource.primary")
    public DataSource primaryDataSource() {
        log.info(">>> Initializing PRIMARY datasource (MySQL)");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory primarySqlSessionFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // XML mapper location
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/primary/*.xml"));

        // MyBatis configuration
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true); // created_at → createdAt
        config.setDefaultFetchSize(100);
        config.setDefaultStatementTimeout(30);
        config.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        factory.setConfiguration(config);

        // Type alias package (dùng trong XML resultType="User")
        factory.setTypeAliasesPackage("com.sakata.boilerplate.models");

        return factory.getObject();
    }

    @Bean("primarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate primarySqlSessionTemplate(
            @Qualifier("primarySqlSessionFactory") SqlSessionFactory factory) {
        return new SqlSessionTemplate(factory);
    }

    @Bean("primaryTransactionManager")
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
