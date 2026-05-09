package fr.cdrochon.thymeleaffrontend.validation;

import fr.cdrochon.thymeleaffrontend.dtos.client.AdresseClientDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.TypeDocumentDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierStatusThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageAdresseDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;

final class FrontendDtoFixtures {

    private FrontendDtoFixtures() {
    }

    static AdresseClientDTO validAdresseClient(int i) {
        return new AdresseClientDTO(
                String.valueOf(10 + (i % 90)),
                "Rue" + i,
                "Bat" + i,
                String.format("%05d", 10000 + (i % 89999)),
                "Ville" + i,
                PaysDTO.FRANCE
        );
    }

    static ClientThymDTO validClient(int i) {
        ClientThymDTO dto = new ClientThymDTO();
        dto.setId("cli-" + i);
        dto.setNomClient("Nom" + i);
        dto.setPrenomClient("Prenom" + i);
        dto.setMailClient("client" + i + "@mail.com");
        dto.setTelClient(String.format("06%08d", i % 100000000));
        dto.setAdresse(validAdresseClient(i));
        dto.setClientStatus(ClientStatusDTO.ACTIF);
        dto.setVehicule(null);
        return dto;
    }

    static DocumentThymDTO validDocument(int i) {
        return DocumentThymDTO.builder()
                .id("doc-" + i)
                .nomDocument("NomDocument" + i)
                .titreDocument("TitreDocument" + i)
                .emetteurDuDocument("Emetteur" + i)
                .typeDocument(TypeDocumentDTO.DEVIS)
                .dateCreationDocument("2026-05-09")
                .dateModificationDocument("2026-05-10")
                .documentStatus(DocumentStatusDTO.CREATED)
                .build();
    }

    static VehiculeThymDTO validVehicule(int i) {
        return VehiculeThymDTO.builder()
                .id("veh-" + i)
                .immatriculationVehicule(String.format("AB-%03d-CD", i % 1000))
                .dateMiseEnCirculationVehicule("2020-01-01")
                .vehiculeStatus(VehiculeStatusDTO.EN_CIRCULATION)
                .client(null)
                .build();
    }

    static DossierThymDTO validDossier(int i) {
        return DossierThymDTO.builder()
                .id("dos-" + i)
                .nomDossier("Dossier" + i)
                .dateCreationDossier("2026-05-09")
                .dateModificationDossier("2026-05-09")
                .client(validClient(i))
                .vehicule(validVehicule(i))
                .dossierStatus(DossierStatusThymDTO.OUVERT)
                .build();
    }

    static GarageAdresseDTO validGarageAdresse(int i) {
        return new GarageAdresseDTO(
                String.valueOf(1 + (i % 100)),
                "RueGarage" + i,
                String.format("%05d", 10000 + (i % 89999)),
                "VilleGarage" + i
        );
    }

    static GaragePostDTO validGarage(int i) {
        return GaragePostDTO.builder()
                .id("gar-" + i)
                .nomGarage("Garage" + i)
                .mailResp("garage" + i + "@mail.com")
                .adresse(validGarageAdresse(i))
                .build();
    }
}

