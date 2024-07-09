package fr.cdrochon.smamonolithe.configuration;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomObjectMapper {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule to handle Java 8 Date/Time types
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Augmenter la profondeur maximale
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                                                                 .maxNestingDepth(2000) // Modifier cette valeur selon vos besoins
                                                                 .build();
        mapper.getFactory().setStreamReadConstraints(constraints);
        
        return mapper;
    }
}
