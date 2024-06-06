//package fr.cdrochon.smamonolithe.event.configuration;
//
//import jakarta.persistence.EntityManagerFactory;
//import org.axonframework.common.jdbc.PersistenceExceptionResolver;
//import org.axonframework.common.jpa.EntityManagerProvider;
//import org.axonframework.common.transaction.TransactionManager;
//import org.axonframework.eventhandling.EventBus;
//import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
//import org.axonframework.eventsourcing.eventstore.EventStore;
//import org.axonframework.eventsourcing.eventstore.jdbc.JdbcSQLErrorCodesResolver;
//import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
//import org.axonframework.serialization.Serializer;
//import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
//import org.axonframework.springboot.autoconfig.AxonServerBusAutoConfiguration;
//import org.axonframework.springboot.util.RegisterDefaultEntities;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.AutoConfigureAfter;
//import org.springframework.boot.autoconfigure.AutoConfigureBefore;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
///**
// * Autoconfiguration class for Axon's JPA specific event store components.
// *
// * @author Sara Pelligrini
// * @since 4.0
// */
////@AutoConfiguration
////@ConditionalOnBean(EntityManagerFactory.class)
////@ConditionalOnMissingBean({EventStorageEngine.class, EventBus.class, EventStore.class})
////@AutoConfigureAfter({AxonServerBusAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
////@AutoConfigureBefore(AxonAutoConfiguration.class)
////@RegisterDefaultEntities(packages = {
////        "org.axonframework.eventsourcing.eventstore.jpa"
////})
//@Configuration
//public class JpaEventStoreAutoConfiguration {
//
//    @Bean
//    public EventStorageEngine eventStorageEngine(Serializer defaultSerializer,
//                                                 PersistenceExceptionResolver persistenceExceptionResolver,
//                                                 @Qualifier("eventSerializer") Serializer eventSerializer,
//                                                 org.axonframework.config.Configuration configuration,
//                                                 EntityManagerProvider entityManagerProvider,
//                                                 TransactionManager transactionManager) {
//        return JpaEventStorageEngine.builder()
//                                    .snapshotSerializer(defaultSerializer)
//                                    .upcasterChain(configuration.upcasterChain())
//                                    .persistenceExceptionResolver(persistenceExceptionResolver)
//                                    .eventSerializer(eventSerializer)
//                                    .snapshotFilter(configuration.snapshotFilter())
//                                    .entityManagerProvider(entityManagerProvider)
//                                    .transactionManager(transactionManager)
//                                    .build();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean({PersistenceExceptionResolver.class, EventStore.class})
//    public PersistenceExceptionResolver jdbcSQLErrorCodesResolver() {
//        return new JdbcSQLErrorCodesResolver();
//    }
//
//}
