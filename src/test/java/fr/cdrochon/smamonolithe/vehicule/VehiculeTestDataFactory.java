package fr.cdrochon.smamonolithe.vehicule;

import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;

import java.time.Instant;

public final class VehiculeTestDataFactory {

    private VehiculeTestDataFactory() {
    }

    public static Instant sampleInstant() {
        return Instant.parse("2026-01-10T10:15:30Z");
    }

    public static VehiculeCommandDTO sampleVehiculeCommandDTO() {
        return VehiculeCommandDTO.builder()
                .id("veh-1")
                .immatriculationVehicule("AA-123-BB")
                .dateMiseEnCirculationVehicule(sampleInstant())
                .vehiculeStatus(VehiculeStatus.EN_CIRCULATION)
                .build();
    }

    public static VehiculeCreatedEvent sampleVehiculeCreatedEvent() {
        return new VehiculeCreatedEvent(
                "veh-1",
                "AA-123-BB",
                sampleInstant(),
                VehiculeStatus.EN_CIRCULATION
        );
    }

    public static Vehicule sampleVehicule() {
        return Vehicule.builder()
                .id("veh-1")
                .immatriculationVehicule("AA-123-BB")
                .dateMiseEnCirculationVehicule(sampleInstant())
                .vehiculeStatus(VehiculeStatus.EN_CIRCULATION)
                .build();
    }

    public static Vehicule sampleVehicule(String id, String immatriculation, VehiculeStatus status) {
        return Vehicule.builder()
                .id(id)
                .immatriculationVehicule(immatriculation)
                .dateMiseEnCirculationVehicule(sampleInstant())
                .vehiculeStatus(status)
                .build();
    }

    public static Client sampleClient() {
        return Client.builder()
                .id("client-1")
                .nomClient("Dupont")
                .prenomClient("Jean")
                .mailClient("jean.dupont@mail.com")
                .telClient("0601020304")
                .clientStatus(ClientStatus.ACTIF)
                .adresse(AdresseClient.builder()
                        .numeroDeRue("12")
                        .rue("Rue de la Paix")
                        .complementAdresse("Apt 3")
                        .cp("75001")
                        .ville("Paris")
                        .pays(Pays.FRANCE)
                        .build())
                .build();
    }
}

