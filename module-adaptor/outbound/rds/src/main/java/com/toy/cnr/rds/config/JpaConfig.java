package com.toy.cnr.rds.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * JPA 관련 Bean 설정.
 * <p>
 * - JPA Repository 스캔 범위를 rds 패키지로 한정
 * - Entity 스캔 범위를 rds 패키지로 한정
 * - 트랜잭션 관리 활성화
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.toy.cnr.rds")
@EntityScan(basePackages = "com.toy.cnr.rds")
@EnableTransactionManagement
public class JpaConfig {

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        DataSource dataSource,
        HibernateSettings hibernateSettings,
        HibernateProperties hibernateProperties
    ) {
        final var properties = hibernateProperties.determineHibernateProperties(
            jpaProperties().getProperties(),
            hibernateSettings
        );

        return builder.dataSource(dataSource)
            .properties(properties)
            .persistenceUnit("base")
            .packages("com.toy.cnr.rds")
            .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(
        EntityManagerFactoryBuilder builder,
        DataSource dataSource,
        HibernateSettings hibernateSettings,
        HibernateProperties hibernateProperties
    ) {
        return new JpaTransactionManager(entityManagerFactory(
            builder,
            dataSource,
            hibernateSettings,
            hibernateProperties
        ).getObject());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa.properties.hibernate")
    public HibernateSettings hibernateSettings() {
        return new HibernateSettings();
    }
}
