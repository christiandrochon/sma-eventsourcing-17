package fr.cdrochon.thymeleaffrontend.ui.validation;

import fr.cdrochon.thymeleaffrontend.entity.garage.AdresseGarage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehiculeForm {
    
    @NotBlank(message = "champ obligatoire")
        @Size(min = 9, max = 9)
    private String immatriculationVehicule;
    
    @NotBlank(message = "champ obligatoire")
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String dateMiseEnCirculationVehicule;

}
