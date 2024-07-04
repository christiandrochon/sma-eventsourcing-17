package fr.cdrochon.thymeleaffrontend.entity.document;

import jakarta.persistence.Embeddable;
import lombok.*;

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
