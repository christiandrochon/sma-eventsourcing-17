//package fr.cdrochon.smamonolithe.event.configuration;
//
//import jakarta.persistence.EntityManager;
//import org.axonframework.common.jdbc.ConnectionProvider;
//import org.axonframework.common.jdbc.PersistenceExceptionResolver;
//import org.axonframework.common.jpa.EntityManagerProvider;
//import org.axonframework.common.transaction.TransactionManager;
//import org.axonframework.config.Configurer;
//import org.axonframework.config.DefaultConfigurer;
//import org.axonframework.eventhandling.EventBus;
//import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
//import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
//import org.axonframework.eventsourcing.eventstore.EventStore;
//import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
//import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
//import org.axonframework.eventsourcing.eventstore.jdbc.JdbcSQLErrorCodesResolver;
//import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
//import org.axonframework.queryhandling.QueryBus;
//import org.axonframework.queryhandling.QueryUpdateEmitter;
//import org.axonframework.queryhandling.SimpleQueryBus;
//import org.axonframework.serialization.Serializer;
//import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean;
//import org.axonframework.tracing.SpanFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//class AxonConfig {
////        @Bean
////        public EventStorageEngine eventStorageEngine(EntityManager entityManager) {
////            return JpaEventStorageEngine.builder()
////                                        .entityManagerProvider(() -> entityManager)
////                                        .build();
////        }
//
//    @Bean
//    public SpringAggregateSnapshotterFactoryBean snapshotter() {
//        return new SpringAggregateSnapshotterFactoryBean();
//    }
//
//    // omitting other configuration methods...
////    @Bean
////    public QueryBus queryBus(SpanFactory spanFactory,
////                             TransactionManager transactionManager,
////                             QueryUpdateEmitter updateEmitter) {
////        return SimpleQueryBus.builder()
////                             .transactionManager(transactionManager)
////                             .spanFactory(spanFactory)
////                             .queryUpdateEmitter(updateEmitter)
////                             .build();
////    }
//
//    @Bean
//    public EventStore eventStore(EventStorageEngine storageEngine) { //,
//        // GlobalMetricRegistry metricRegistry) {
//        return EmbeddedEventStore.builder()
//                                 .storageEngine(storageEngine)
//                                 //                                 .messageMonitor(metricRegistry.registerEventBus("eventStore"))
//                                 //                                 .spanFactory(spanFactory)
//                                 .build();
//    }
//
//
////    public Configurer jpaEventStorageConfigurer(EntityManagerProvider entityManagerProvider,
////                                                TransactionManager transactionManager) {
////        return DefaultConfigurer.jpaConfiguration(entityManagerProvider, transactionManager);
////    }
//
//    // The JpaEventStorageEngine stores events in a JPA-compatible data source.
////    @Bean
////    public EventStorageEngine eventStorageEngine(Serializer serializer,
////                                                 PersistenceExceptionResolver persistenceExceptionResolver,
////                                                 @Qualifier("eventSerializer") Serializer eventSerializer,
////                                                 EntityManagerProvider entityManagerProvider,
////                                                 TransactionManager transactionManager) {
////        return JpaEventStorageEngine.builder()
////                                    .snapshotSerializer(serializer)
////                                    .persistenceExceptionResolver(persistenceExceptionResolver)
////                                    .eventSerializer(eventSerializer)
////                                    .entityManagerProvider(entityManagerProvider)
////                                    .transactionManager(transactionManager)
////                                    .build();
////    }
//
//    @Bean
//    public PersistenceExceptionResolver persistenceExceptionResolver() {
//        return new JdbcSQLErrorCodesResolver();
//    }
//
//    //    @Bean
//    //    @ConditionalOnMissingBean({EventStorageEngine.class, EventBus.class, EventStore.class})
//    //    public EventStorageEngine eventStorageEngine(Serializer defaultSerializer,
//    //                                                 PersistenceExceptionResolver persistenceExceptionResolver,
//    //                                                 @Qualifier("eventSerializer") Serializer eventSerializer,
//    //                                                 org.axonframework.config.Configuration configuration,
//    //                                                 ConnectionProvider connectionProvider,
//    //                                                 TransactionManager transactionManager,
//    //                                                 EventSchema eventSchema) {
//    //        return JdbcEventStorageEngine.builder()
//    //                                     .snapshotSerializer(defaultSerializer)
//    //                                     .upcasterChain(configuration.upcasterChain())
//    //                                     .persistenceExceptionResolver(persistenceExceptionResolver)
//    //                                     .eventSerializer(eventSerializer)
//    //                                     .snapshotFilter(configuration.snapshotFilter())
//    //                                     .connectionProvider(connectionProvider)
//    //                                     .transactionManager(transactionManager)
//    //                                     .schema(eventSchema)
//    //                                     .build();
//    //    }
//
//    @Bean
//    @ConditionalOnMissingBean({PersistenceExceptionResolver.class, EventStore.class})
//    public PersistenceExceptionResolver jdbcSQLErrorCodesResolver() {
//        return new JdbcSQLErrorCodesResolver();
//    }
//}
//
//
