//package fr.cdrochon.smamonolithe.document.query.entities;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.io.IOException;
//
//public class TypeDocumentDeserializer extends JsonDeserializer<TypeDocument> {
//    @Override
//    public TypeDocument deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
//        String nomTypeDocument = node.asText();
//        return TypeDocument.PREDEFINED_VALUES.stream()
//                                             .filter(typeDocument -> typeDocument.getNomTypeDocument().equals(nomTypeDocument))
//                                             .findFirst()
//                                             .orElseThrow(() -> new IOException("Unknown TypeDocument: " + nomTypeDocument));
//    }
//}
