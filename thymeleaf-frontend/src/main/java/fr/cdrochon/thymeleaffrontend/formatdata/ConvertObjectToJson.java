package fr.cdrochon.thymeleaffrontend.formatdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertObjectToJson {
    
    /**
     * Convertit un objet en chaîne de caractères JSON
     * @param obj Objet à convertir
     * @return Chaîne de caractères JSON
     */
    public static String convertObjectToJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(obj);
            return json;
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

}
