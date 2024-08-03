package fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv;

import lombok.*;

import java.util.Arrays;
import java.util.Collection;

//TODO 140 marques à saisir

@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class MarqueVehiculeDTO {
    
    Collection<String> marques = Arrays.asList("ABARTH",
                                               "ADRIA",
                                               "ALFA_ROMEO",
                                               "AUDI",
                                               "BMW",
                                               "CADILLAC",
                                               "DACIA",
                                               "DUCATI",
                                               "FIAT",
                                               "FORD",
                                               "HARLEY_DAVIDSON",
                                               "HONDA",
                                               "HYUNDAI",
                                               "ISUZU",
                                               "JEEP",
                                               "KIA",
                                               "LADA",
                                               "LANCIA",
                                               "MATRA",
                                               "MERCEDES",
                                               "OPEL",
                                               "RENAULT",
                                               "ROVER",
                                               "SKODA",
                                               "SMART",
                                               "SUZUKI",
                                               "TOYOTA",
                                               "VOLKSWAGEN",
                                               "VOLVO",
                                               "YAMAHA",
                                               "NON_DISPONIBLE");
    
    
}
