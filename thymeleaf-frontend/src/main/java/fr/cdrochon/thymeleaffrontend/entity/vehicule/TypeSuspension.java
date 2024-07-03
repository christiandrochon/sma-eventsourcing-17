package fr.cdrochon.thymeleaffrontend.entity.vehicule;

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
public class TypeSuspension {
    
    Collection<String> typeSuspension = Arrays.asList("CLASSIQUE", "PNEUMATIQUE", "HYDROPNEUMATIQUE");

}
