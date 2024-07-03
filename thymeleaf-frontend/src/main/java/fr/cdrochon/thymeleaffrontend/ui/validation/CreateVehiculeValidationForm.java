package fr.cdrochon.thymeleaffrontend.ui.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehiculeValidationForm {
    
//    @NotBlank(message = "champ obligatoire")
//        @Size(min = 9, max = 9)
    private String immatriculationVehicule;
    
    @NotBlank(message = "champ obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Instant dateMiseEnCirculationVehicule;

}
