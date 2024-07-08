package fr.cdrochon.smamonolithe.document.query.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

//@Entity
@Embeddable
@Builder
@Getter
//@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeDocument {

//    private String id;
    private String nomTypeDocument;
    
    public static final TypeDocument DEVIS = new TypeDocument("DEVIS");
    public static final TypeDocument FACTURE = new TypeDocument("FACTURE");
    
    // Liste des valeurs prédéfinies
    public static final Collection<TypeDocument> PREDEFINED_VALUES = Collections.unmodifiableList(Arrays.asList(DEVIS, FACTURE));
}
