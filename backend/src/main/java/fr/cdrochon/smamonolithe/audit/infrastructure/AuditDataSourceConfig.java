package fr.cdrochon.smamonolithe.audit.infrastructure;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration du DataSource dédié à la base d'audit RGPD.
 *
 * <p>Ce DataSource est distinct du DataSource principal (monolithe).
 * Il pointe vers la base {@code audit} et utilise un {@link NamedParameterJdbcTemplate}
 * propre pour garantir qu'aucune opération JPA/Hibernate ne peut altérer les données d'audit.</p>
 *
 * <p>Les propriétés sont lues depuis le préfixe {@code audit.datasource.*} dans
 * {@code application.properties} / {@code application-local.properties}.</p>
 */
@Configuration
public class AuditDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(@Qualifier("mainDataSourceProperties") DataSourceProperties mainDataSourceProperties) {
        return mainDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "audit.datasource")
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * DataSource pointant sur la base {@code audit}.
     * Utilise HikariCP (pool par défaut de Spring Boot).
     */
    @Bean(name = "auditDataSource")
    @ConfigurationProperties(prefix = "audit.datasource.hikari")
    public DataSource auditDataSource(@Qualifier("auditDataSourceProperties") DataSourceProperties auditDataSourceProperties) {
        // DataSourceProperties gere correctement la conversion url -> jdbcUrl pour Hikari.
        return auditDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * JdbcTemplate nommé utilisant exclusivement le DataSource audit.
     * L'AuditService l'injecte par son nom de bean pour éviter toute ambiguïté.
     */
    @Bean(name = "auditJdbcTemplate")
    public NamedParameterJdbcTemplate auditJdbcTemplate(@Qualifier("auditDataSource") DataSource auditDataSource) {
        return new NamedParameterJdbcTemplate(auditDataSource);
    }
}

