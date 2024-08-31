package fr.cdrochon.thymeleaffrontend.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ConvertObjectToJson {
    
    /**
     * Convertit un objet en chaîne de caractères JSON. L'objet prend en compte les dates de type Instant.
     *
     * @param obj Objet à convertir
     * @return Chaîne de caractères JSON
     */
    public static String convertObjectToJson(Object obj) {
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        
        String json;
        try {
            json = objectMapper.writeValueAsString(obj);
            return json;
        } catch(JsonProcessingException e) {
            return e.getMessage();
        }
    }
    
}
