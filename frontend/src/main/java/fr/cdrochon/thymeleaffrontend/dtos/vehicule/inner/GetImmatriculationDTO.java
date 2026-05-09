package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetImmatriculationDTO {
    
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Format incorrect.")
    private String immatriculation;
}
