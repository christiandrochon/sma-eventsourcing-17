package fr.cdrochon.thymeleaffrontend.entity.document;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    public static TypeDocument DEVIS = new TypeDocument("DEVIS");
    public static TypeDocument FACTURE = new TypeDocument("FACTURE");

    // Liste des valeurs prédéfinies
    public static final Collection<TypeDocument> PREDEFINED_VALUES = Collections.unmodifiableList(Arrays.asList(DEVIS, FACTURE));
    
//    Collection<String> PREDEFINED_VALUES = Arrays.asList("FACTURE", "FACTURE");
}
