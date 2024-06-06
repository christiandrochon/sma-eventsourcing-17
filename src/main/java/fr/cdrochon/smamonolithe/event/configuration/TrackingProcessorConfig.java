//package fr.cdrochon.smamonolithe.event.configuration;
//
//import fr.cdrochon.smamonolithe.event.command.aggregate.GarageQueryAggregate;
//import org.axonframework.config.ConfigurerModule;
//import org.axonframework.config.DefaultConfigurer;
//import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
//import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
//import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
//import org.axonframework.messaging.StreamableMessageSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
////@ComponentScan()
//public class TrackingProcessorConfig {
//    @Bean
//    public ConfigurerModule configureOrderProcessor() {
//        TrackingEventProcessorConfiguration tepConfig =
//                TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
//                                                   .andInitialTrackingToken(StreamableMessageSource::createHeadToken);
//        return configurer -> configurer.eventProcessing()
//                                       .registerTrackingEventProcessorConfiguration("GarageQueryEventHandlerService", c -> tepConfig);
//    }
//
//
////    CardSummaryProjection projection = new CardSummaryProjection();
////    EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
////eventHandlingConfiguration.registerEventHandler(c -> projection);
////
////    Configuration configuration = DefaultConfigurer.defaultConfiguration()
////                                                   .configureAggregate(GarageQueryAggregate.class) // (1)
////                                                   .configureEventStore(c -> new EmbeddedEventStore(new InMemoryEventStorageEngine())) //(2)
////                                                   .registerModule(eventHandlingConfiguration) // (3)
////                                                   .registerQueryHandler(c -> projection) // (4)
////                                                   .buildConfiguration(); // (5)
//}
