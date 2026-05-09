package fr.cdrochon.smamonolithe.audit.infrastructure;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    /**
     * DataSource pointant sur la base {@code audit}.
     * Utilise HikariCP (pool par défaut de Spring Boot).
     */
    @Bean(name = "auditDataSource")
    @ConfigurationProperties(prefix = "audit.datasource")
    public DataSource auditDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * JdbcTemplate nommé utilisant exclusivement le DataSource audit.
     * L'AuditService l'injecte par son nom de bean pour éviter toute ambiguïté.
     */
    @Bean(name = "auditJdbcTemplate")
    public NamedParameterJdbcTemplate auditJdbcTemplate() {
        return new NamedParameterJdbcTemplate(auditDataSource());
    }
}

