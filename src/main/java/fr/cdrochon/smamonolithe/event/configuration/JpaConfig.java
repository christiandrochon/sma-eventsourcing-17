package fr.cdrochon.smamonolithe.event.configuration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configurer;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.micrometer.GlobalMetricRegistry;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerBusAutoConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.sql.SQLException;

@AutoConfiguration
@ConditionalOnBean(EntityManagerFactory.class)
@ConditionalOnMissingBean({EventStorageEngine.class, EventBus.class, EventStore.class})
@AutoConfigureAfter({AxonServerBusAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureBefore(AxonAutoConfiguration.class)
@RegisterDefaultEntities(packages = {
        "org.axonframework.eventsourcing.eventstore.jpa"
})
//@Configuration
public class JpaConfig {
    
    //    @Bean
    //    public EventStorageEngine eventStorageEngine(EntityManager entityManager, DataSource dataSource) throws SQLException {
    //        return JpaEventStorageEngine.builder()
    //                                    .entityManagerProvider(() -> entityManager)
    //                                    .dataSource(dataSource)
    //                                    .build();
    //    }
    
    
    //Connecter Axon server à postgresql
    //    @Bean
    //    public JpaTokenStore tokenStore(EntityManager entityManager, org.axonframework.config.Configuration configuration) {
    //        return JpaTokenStore.builder()
    //                            .entityManagerProvider(() -> entityManager)
    //                            .serializer(configuration.serializer())
    //                            .build();
    //    }
    
    // The EmbeddedEventStore delegates actual storage and retrieval of events to an EventStorageEngine.
    @Bean
    public EventStore eventStore(EventStorageEngine storageEngine,
                                 GlobalMetricRegistry metricRegistry) {
        return EmbeddedEventStore.builder()
                                 .storageEngine(storageEngine)
                                 .messageMonitor(metricRegistry.registerEventBus("eventStore"))
                                 //                                 .spanFactory(spanFactory)
                                 .build();
    }
    
    @Qualifier("eventSerializer")
    @Bean
    public Serializer eventSerializer() {
        return JacksonSerializer.builder().build();
    }

    
    // The JpaEventStorageEngine stores events in a JPA-compatible data source.
    @Bean
    public EventStorageEngine eventStorageEngine(Serializer serializer,
            //                                                     DataSource dataSource, //+
                                                 PersistenceExceptionResolver persistenceExceptionResolver,
                                                 @Qualifier("eventSerializer") Serializer eventSerializer,
                                                 org.axonframework.config.Configuration configuration, //+
                                                 EntityManagerProvider entityManagerProvider,
                                                 TransactionManager transactionManager) throws SQLException {
        return JpaEventStorageEngine.builder()
                                    .snapshotSerializer(serializer)
                                    .persistenceExceptionResolver(persistenceExceptionResolver)
                                    .eventSerializer(eventSerializer)
                                    .entityManagerProvider(entityManagerProvider)
                                    .transactionManager(transactionManager)
                                    //                                        .dataSource(dataSource) //+
                                    .snapshotFilter(configuration.snapshotFilter()) //+
                                    .upcasterChain(configuration.upcasterChain()) //+
                                    .build();
    }
    
    //    @Bean
    //    public EventStorageEngine eventStorageEngine(EntityManager entityManager,
    //                                                 DataSource dataSource, org.axonframework.config.Configuration axonConfiguration) throws SQLException {
    //        return JpaEventStorageEngine.builder()
    //                                    .entityManagerProvider(() -> entityManager)
    //                                    .dataSource(dataSource)
    //                                    .serializer(axonConfiguration.serializer())
    //                                    .build();
    //    }
    
    
    //    @Bean
    //    public EventStorageEngine storageEngine(Serializer serializer,
    //                                            ConnectionProvider connectionProvider,
    //                                            @Qualifier("eventSerializer") Serializer eventSerializer,
    //                                            TransactionManager transactionManager,
    //                                            org.axonframework.config.Configuration configuration, //+
    //                                            EventTableFactory tableFactory) {
    //        JdbcEventStorageEngine storageEngine = JdbcEventStorageEngine.builder()
    //                                                                     .snapshotSerializer(serializer)
    //                                                                     .connectionProvider(connectionProvider)
    //                                                                     .eventSerializer(eventSerializer)
    //                                                                     .transactionManager(transactionManager)
    //                                                                     .snapshotFilter(configuration.snapshotFilter()) //+
    //                                                                     .upcasterChain(configuration.upcasterChain()) //+
    //                                                                     .build();
    //        // If the schema has not been constructed yet, the createSchema method can be used:
    //        storageEngine.createSchema(tableFactory);
    //        return storageEngine;
    //    }
}
