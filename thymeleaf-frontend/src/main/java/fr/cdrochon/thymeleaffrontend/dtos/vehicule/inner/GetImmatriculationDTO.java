package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetImmatriculationDTO {
    
//    @NotBlank(message = "L'immatriculation du véhicule est obligatoire, merci de la renseigner.")
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Format incorrect.")
    private String immatriculation;
}
