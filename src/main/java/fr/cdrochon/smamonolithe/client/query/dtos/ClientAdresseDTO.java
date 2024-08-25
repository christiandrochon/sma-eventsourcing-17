package fr.cdrochon.smamonolithe.client.query.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString @Builder
public class ClientAdresseDTO {
    
    private String numeroDeRue;
    private String rue;
    private String complementAdresse;
    private String cp;
    private String ville;
    // Enum -> immutable, pas besoin de le convertir en DTO
    private Pays pays;
    
}
