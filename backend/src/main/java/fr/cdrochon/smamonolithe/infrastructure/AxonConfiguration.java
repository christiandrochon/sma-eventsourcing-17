package fr.cdrochon.smamonolithe.infrastructure;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Axon pour les EventHandlers et QueryHandlers
 */
@Configuration
public class AxonConfiguration {

    @Value("${app.axon.default-event-processor-mode:subscribing}")
    private String defaultProcessorMode;

    /**
     * Configure DocumentEventHandlerService avec un TrackingEventProcessor explicite
     * pour s'assurer que les événements Document sont traités et persistés
     */
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        // En local/tests (H2), tracking peut échouer sur token_entry ("for no key update").
        // On utilise subscribing par défaut pour garantir la persistance des projections.
        if ("tracking".equalsIgnoreCase(defaultProcessorMode)) {
            configurer.usingTrackingEventProcessors();
        } else {
            configurer.usingSubscribingEventProcessors();
        }
    }
}

