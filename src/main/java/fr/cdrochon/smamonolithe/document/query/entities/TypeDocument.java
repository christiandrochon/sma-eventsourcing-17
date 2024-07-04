package fr.cdrochon.smamonolithe.document.query.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeDocument {
    @Id
    private String id;
    private String nomTypeDocument;
    
    public static final TypeDocument DEVIS = new TypeDocument("DEVIS", "DEVIS");
    public static final TypeDocument FACTURE = new TypeDocument("FACTURE", "FACTURE");
    
    // Liste des valeurs prédéfinies
    public static final Collection<TypeDocument> PREDEFINED_VALUES = Collections.unmodifiableList(Arrays.asList(DEVIS, FACTURE));
}
