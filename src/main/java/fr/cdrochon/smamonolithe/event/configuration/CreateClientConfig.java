//package fr.cdrochon.smamonolithe.event.configuration;
//
//import org.axonframework.config.ConfigurerModule;
//import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
//import org.axonframework.messaging.StreamableMessageSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class CreateClientConfig {
//    @Bean
//    public ConfigurerModule configurerCreateClient() {
//        TrackingEventProcessorConfiguration trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
//                                                                                                                     .andInitialTrackingToken(
//                                                                                                                             StreamableMessageSource::createHeadToken);
//        return configurer -> configurer.eventProcessing()
//                                       .registerTrackingEventProcessorConfiguration("CreatedClient", configuration -> trackingEventProcessorConfiguration);
//    }
//}
