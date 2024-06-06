//package fr.cdrochon.smamonolithe.event.configuration;
//
//import fr.cdrochon.smamonolithe.event.command.aggregate.GarageQueryAggregate;
//import org.axonframework.config.DefaultConfigurer;
//import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
//import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ComponentScan()
//public class TrackingProcessorConfig {
//
//    CardSummaryProjection projection = new CardSummaryProjection();
//    EventHandlingConfiguration eventHandlingConfiguration = new EventHandlingConfiguration();
//eventHandlingConfiguration.registerEventHandler(c -> projection);
//
//    Configuration configuration = DefaultConfigurer.defaultConfiguration()
//                                                   .configureAggregate(GarageQueryAggregate.class) // (1)
//                                                   .configureEventStore(c -> new EmbeddedEventStore(new InMemoryEventStorageEngine())) //(2)
//                                                   .registerModule(eventHandlingConfiguration) // (3)
//                                                   .registerQueryHandler(c -> projection) // (4)
//                                                   .buildConfiguration(); // (5)
//}
