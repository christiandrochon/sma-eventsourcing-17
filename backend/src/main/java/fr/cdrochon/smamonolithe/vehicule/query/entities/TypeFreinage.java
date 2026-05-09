package fr.cdrochon.smamonolithe.vehicule.query.entities;

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
public class TypeFreinage {
    Collection<String> typeFreinage = Arrays.asList("DISQUES", "TAMBOURS", "DISQUES_ET_TAMBOURS");
    
}
