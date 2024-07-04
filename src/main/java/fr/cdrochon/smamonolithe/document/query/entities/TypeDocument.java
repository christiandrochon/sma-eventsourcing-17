package fr.cdrochon.smamonolithe.document.query.entities;

import lombok.*;

import javax.persistence.Embeddable;
import java.util.Arrays;
import java.util.Collection;

@Embeddable
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeDocument {
    Collection<String> typeDocument = Arrays.asList("DEVIS", "FACTURE");
//    DEVIS, FACTURE
}
