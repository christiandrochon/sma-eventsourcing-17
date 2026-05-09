package fr.cdrochon.thymeleaffrontend.dtos.document;

import lombok.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeDocumentDTO {
    
    private String nomTypeDocument;
    
    public static final TypeDocumentDTO DEVIS = new TypeDocumentDTO("DEVIS");
    public static final TypeDocumentDTO FACTURE = new TypeDocumentDTO("FACTURE");
    
    // Liste des valeurs prédéfinies
    public static final Collection<TypeDocumentDTO> PREDEFINED_VALUES = Collections.unmodifiableList(Arrays.asList(DEVIS, FACTURE));
}
