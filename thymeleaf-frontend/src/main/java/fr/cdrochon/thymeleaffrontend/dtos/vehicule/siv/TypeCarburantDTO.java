package fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv;

import lombok.*;

import java.util.Arrays;
import java.util.Collection;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeCarburantDTO {
    
    Collection<String> typeCarburant = Arrays.asList("ESSENCE", "DIESEL", "ELECTRIQUE", "HYBRIDE");
    
}
