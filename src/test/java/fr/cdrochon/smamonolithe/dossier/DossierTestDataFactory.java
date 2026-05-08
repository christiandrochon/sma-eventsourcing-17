package fr.cdrochon.smamonolithe.dossier;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;

import java.time.Instant;

public final class DossierTestDataFactory {

    public static final String DOSSIER_ID = "dossier-1";
    public static final String CLIENT_ID = "client-1";
    public static final String VEHICULE_ID = "vehicule-1";
    public static final Instant CREATED_AT = Instant.parse("2025-01-01T10:00:00Z");
    public static final Instant UPDATED_AT = Instant.parse("2025-01-01T11:00:00Z");

    private DossierTestDataFactory() {
    }

    public static ClientAdresseDTO sampleAdresseDto() {
        return ClientAdresseDTO.builder()
                .numeroDeRue("10")
                .rue("Rue de Paris")
                .complementAdresse("Bat A")
                .cp("75001")
                .ville("Paris")
                .pays(Pays.FRANCE)
                .build();
    }

    public static AdresseClient sampleAdresseEntity() {
        return AdresseClient.builder()
                .numeroDeRue("10")
                .rue("Rue de Paris")
                .complementAdresse("Bat A")
                .cp("75001")
                .ville("Paris")
                .pays(Pays.FRANCE)
                .build();
    }

    public static ClientCommandDTO sampleClientCommandDto() {
        return ClientCommandDTO.builder()
                .id(CLIENT_ID)
                .nomClient("Dupont")
                .prenomClient("Jean")
                .mailClient("jean.dupont@mail.com")
                .telClient("0600000000")
                .adresse(sampleAdresseDto())
                .clientStatus(ClientStatus.ACTIF)
                .build();
    }

    public static VehiculeCommandDTO sampleVehiculeCommandDto() {
        return VehiculeCommandDTO.builder()
                .id(VEHICULE_ID)
                .immatriculationVehicule("AA-123-BB")
                .dateMiseEnCirculationVehicule(CREATED_AT)
                .vehiculeStatus(VehiculeStatus.EN_CIRCULATION)
                .build();
    }

    public static Client sampleClientEntity() {
        Client client = Client.builder()
                .id(CLIENT_ID)
                .nomClient("Dupont")
                .prenomClient("Jean")
                .mailClient("jean.dupont@mail.com")
                .telClient("0600000000")
                .adresse(sampleAdresseEntity())
                .clientStatus(ClientStatus.ACTIF)
                .build();
        client.setVehicule(null);
        return client;
    }

    public static Vehicule sampleVehiculeEntity() {
        Vehicule vehicule = Vehicule.builder()
                .id(VEHICULE_ID)
                .immatriculationVehicule("AA-123-BB")
                .dateMiseEnCirculationVehicule(CREATED_AT)
                .vehiculeStatus(VehiculeStatus.EN_CIRCULATION)
                .build();
        vehicule.setClient(null);
        return vehicule;
    }

    public static DossierCommandDTO sampleDossierCommandDto() {
        return DossierCommandDTO.builder()
                .id(DOSSIER_ID)
                .nomDossier("DOSSIER-001")
                .dateCreationDossier(CREATED_AT)
                .dateModificationDossier(UPDATED_AT)
                .client(sampleClientCommandDto())
                .vehicule(sampleVehiculeCommandDto())
                .dossierStatus(DossierStatus.OUVERT)
                .build();
    }

    public static DossierCreateCommand sampleDossierCreateCommand() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();
        return new DossierCreateCommand(
                DOSSIER_ID,
                "DOSSIER-001",
                CREATED_AT,
                UPDATED_AT,
                client,
                vehicule,
                DossierStatus.OUVERT,
                client.getId(),
                vehicule.getId()
        );
    }

    public static DossierCreatedEvent sampleDossierCreatedEvent() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();
        return new DossierCreatedEvent(
                DOSSIER_ID,
                "DOSSIER-001",
                CREATED_AT,
                UPDATED_AT,
                client,
                vehicule,
                DossierStatus.OUVERT,
                client.getId(),
                vehicule.getId()
        );
    }

    public static Dossier sampleDossierEntity() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();
        Dossier dossier = Dossier.builder()
                .id(DOSSIER_ID)
                .nomDossier("DOSSIER-001")
                .dateCreationDossier(CREATED_AT)
                .dateModificationDossier(UPDATED_AT)
                .client(client)
                .vehicule(vehicule)
                .dossierStatus(DossierStatus.OUVERT)
                .build();
        client.setDossier(dossier);
        client.setVehicule(vehicule);
        vehicule.setDossier(dossier);
        vehicule.setClient(client);
        return dossier;
    }
}

