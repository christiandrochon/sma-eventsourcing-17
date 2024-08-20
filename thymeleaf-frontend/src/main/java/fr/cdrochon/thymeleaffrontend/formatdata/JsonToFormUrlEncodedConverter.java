package fr.cdrochon.thymeleaffrontend.formatdata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonToFormUrlEncodedConverter {
    
    /**
     * Convertit un objet JSON en une chaîne de caractères au format x-www-form-urlencoded
     *
     * @param obj Objet à convertir
     * @return Chaîne de caractères au format x-www-form-urlencoded
     *
     * @throws JsonProcessingException      exception levée lors de la conversion de l'objet en JSON
     * @throws UnsupportedEncodingException exception levée lors de l'encodage des caractères
     */
    public static String convertToFormUrlEncoded(Object obj) throws JsonProcessingException, UnsupportedEncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
        StringBuilder formUrlEncoded = new StringBuilder();
        
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            if(formUrlEncoded.length() > 0) {
                formUrlEncoded.append("&");
            }
            formUrlEncoded.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                          .append("=")
                          .append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }
        
        return formUrlEncoded.toString();
    }
}
