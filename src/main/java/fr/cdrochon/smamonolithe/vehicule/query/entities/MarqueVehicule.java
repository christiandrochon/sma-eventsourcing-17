package fr.cdrochon.smamonolithe.vehicule.query.entities;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

//TODO 140 marques à saisir
@Embeddable
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class MarqueVehicule {
    
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
