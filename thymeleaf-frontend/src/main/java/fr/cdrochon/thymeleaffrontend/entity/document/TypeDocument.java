package fr.cdrochon.thymeleaffrontend.entity.document;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

//@Entity
@Embeddable
//@Builder
@Getter
////@Setter
@NoArgsConstructor
@AllArgsConstructor
//@ToString
public class TypeDocument {
//    @Id
//    private String id;
    private String nomTypeDocument;
//
    public static final TypeDocument DEVIS = new TypeDocument("DEVIS");
    public static final TypeDocument FACTURE = new TypeDocument("FACTURE");

    // Liste des valeurs prédéfinies
    public static final Collection<TypeDocument> PREDEFINED_VALUES = Collections.unmodifiableList(Arrays.asList(DEVIS, FACTURE));
    
//    Collection<String> PREDEFINED_VALUES = Arrays.asList("FACTURE", "FACTURE");
}
