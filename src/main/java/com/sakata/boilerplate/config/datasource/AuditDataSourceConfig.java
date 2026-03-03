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
 * Datasource AUDIT → MySQL
 * Chứa: audit_logs (ghi lại toàn bộ thao tác)
 *
 * Scan mapper: com.sakata.boilerplate.mapper.audit
 * XML mapper:  classpath:mapper/audit/*.xml
 */
@Configuration
@EnableTransactionManagement
@MapperScan(
    basePackages = "com.sakata.boilerplate.mapper.audit",
    sqlSessionFactoryRef = "auditSqlSessionFactory"
)
@Slf4j
public class AuditDataSourceConfig {

    @Bean("auditDataSource")
    @ConfigurationProperties(prefix = "datasource.audit")
    public DataSource auditDataSource() {
        log.info(">>> Initializing AUDIT datasource (MySQL)");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("auditSqlSessionFactory")
    public SqlSessionFactory auditSqlSessionFactory(
        @Qualifier("auditDataSource") DataSource dataSource
    ) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        factory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/audit/*.xml")
        );

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        config.setDefaultFetchSize(50);
        config.setDefaultStatementTimeout(30);
        config.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        factory.setConfiguration(config);

        factory.setTypeAliasesPackage("com.sakata.boilerplate.audit.models");

        return factory.getObject();
    }

    @Bean("auditSqlSessionTemplate")
    public SqlSessionTemplate auditSqlSessionTemplate(
        @Qualifier("auditSqlSessionFactory") SqlSessionFactory factory
    ) {
        return new SqlSessionTemplate(factory);
    }

    @Bean("auditTransactionManager")
    public PlatformTransactionManager auditTransactionManager(
        @Qualifier("auditDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }
}