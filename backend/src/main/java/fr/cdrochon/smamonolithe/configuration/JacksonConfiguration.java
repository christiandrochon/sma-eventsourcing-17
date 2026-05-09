package fr.cdrochon.smamonolithe.configuration;

//import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    
//    public CustomObjectMapper() {
//        // Register JavaTimeModule to handle Java 8 Date/Time types
//        registerModule(new JavaTimeModule());
//        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        this.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
//        this.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
//
//        // Augmenter la profondeur maximale
//        StreamReadConstraints constraints = StreamReadConstraints.builder()
//                                                                 .maxNestingDepth(2000) // Modifier cette valeur selon vos besoins
//                                                                 .build();
//        getFactory().setStreamReadConstraints(constraints);
//    }
//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//
//        // Register JavaTimeModule to handle Java 8 Date/Time types
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.disable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
////        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
////        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
//
//        // Augmenter la profondeur maximale
//        StreamReadConstraints constraints = StreamReadConstraints.builder()
//                                                                 .maxNestingDepth(2000) // Modifier cette valeur selon vos besoins
//                                                                 .build();
//        mapper.getFactory().setStreamReadConstraints(constraints);
//
//        return mapper;
//    }
}
