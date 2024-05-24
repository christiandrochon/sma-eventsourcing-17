package fr.cdrochon.smamonolithe;

import fr.cdrochon.smamonolithe.client.entity.Client;
import fr.cdrochon.smamonolithe.client.repository.ClientRepository;
import fr.cdrochon.smamonolithe.document.entity.Document;
import fr.cdrochon.smamonolithe.document.entity.TypeDocument;
import fr.cdrochon.smamonolithe.document.repository.DocumentRepository;
import fr.cdrochon.smamonolithe.garage.entity.Adresse;
import fr.cdrochon.smamonolithe.garage.entity.Garage;
import fr.cdrochon.smamonolithe.garage.repository.GarageRepository;
import fr.cdrochon.smamonolithe.vehicule.entity.MarqueVehicule;
import fr.cdrochon.smamonolithe.vehicule.entity.TypeVehicule;
import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.repository.VehiculeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class SmaMonolitheApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SmaMonolitheApplication.class, args);
    }
    
    @Bean
    CommandLineRunner commandLineRunner(GarageRepository garageRepository,
                                        ClientRepository clientRepository,
                                        VehiculeRepository vehiculeRepository,
                                        DocumentRepository documentRepository) {
        return args -> {
            
            Adresse adresse1 = Adresse.builder()
                                      .rue("rue du truc")
                                      .numeroDeRue("17")
                                      .cp("33300")
                                      .ville("St Jean")
                                      .build();
            Adresse adresse2 = Adresse.builder()
                                      .rue("avenue du jo")
                                      .cp("59140")
                                      .numeroDeRue("51")
                                      .ville(
                                              "Cloerwak")
                                      .build();
            
            List<Garage> garageList = List.of(
                    Garage.builder()
                          .nomGarage("Garage du Cres")
                          .emailContactGarage("garageDuCres@gmail.com")
                          .adresseGarage(adresse1)
                          .build(),
                    Garage.builder()
                          .nomGarage("Garage du louga")
                          .emailContactGarage("garageLouge@toto.net")
                          .adresseGarage(adresse2)
                          .build()
                                             );
            garageRepository.saveAll(garageList);
            
            
            //            List<Client> customerList = List.of(
            Client client1 = Client.builder()
                                   .id(1L)
                                   .nomClient("Dubourg")
                                   .prenomClient("Jean")
                                   .mailClient("jean.dubourg@gmail.com")
                                   .telClient("0123456789")
                                   .adresseClient(adresse1)
                                   .build();
            Client client2 = Client.builder()
                                   .id(2L)
                                   .nomClient("Berga")
                                   .prenomClient("Christine")
                                   .mailClient("chirstine.berga@tut.io")
                                   .telClient("9876541320")
                                   .adresseClient(adresse2)
                                   .build();
            Client client3 = Client.builder()
                                   .id(3L)
                                   .nomClient("Poloua")
                                   .prenomClient("Gerard")
                                   .mailClient("gegelembrouille@tut.io")
                                   .telClient("8888888888")
                                   .adresseClient(adresse2)
                                   .build();
            //            );
            //            clientRepository.saveAll(customerList);
            clientRepository.save(client1);
            clientRepository.save(client2);
            clientRepository.save(client3);
            
            
            Vehicule vehicule1 = Vehicule.builder()
                                         .immatriculationVehicule("PD-158-DZ")
                                         .dateMiseEnCirculationVehicule(LocalDate.now())
                                         .climatisationVehicule(true)
                                         .typeVehicule(TypeVehicule.VOITURE)
                                         .marqueVehicule(MarqueVehicule.BMW)
                                         //.clientId(1L)
                                         .clientId(client2.getId())
                                         .build();
            Vehicule vehicule2 = Vehicule.builder()
                                         .immatriculationVehicule("OS-184-PS")
                                         .dateMiseEnCirculationVehicule(LocalDate.of(2020, 12, 14))
                                         .climatisationVehicule(false)
                                         .typeVehicule(TypeVehicule.MOTO)
                                         .marqueVehicule(MarqueVehicule.HARLEY_DAVIDSON)
                                         //.clientId(2L)
                                         .clientId(client3.getId())
                                         .build();
            Vehicule vehicule3 = Vehicule.builder()
                                         .immatriculationVehicule("SS-147-DG")
                                         .dateMiseEnCirculationVehicule(LocalDate.of(2023, 8, 1))
                                         .climatisationVehicule(true)
                                         .typeVehicule(TypeVehicule.VOITURE)
                                         .marqueVehicule(MarqueVehicule.AUDI)
                                         //.clientId(3L)
                                         .clientId(client1.getId())
                                         .build();
            vehiculeRepository.save(vehicule1);
            vehiculeRepository.save(vehicule2);
            vehiculeRepository.save(vehicule3);
            
            
            Document d1 = Document.builder()
                                  .nomDocument("devis-00012547")
                                  .titreDocument("rdv pour essieu")
                                  .emetteurDuDocument("garage durand")
                                  .typeDocument(TypeDocument.DEVIS)
                                  .dateCreationDocument(LocalDate.now())
                                  .dateModificationDocument(LocalDate.now())
                                  //.vehiculeId(2L)
                                  .vehiculeId(vehicule1.getId())
                                  .build();
            Document d2 = Document.builder()
                                  .nomDocument("facture mr X")
                                  .titreDocument("facture du garage robert")
                                  .emetteurDuDocument("garage robert")
                                  .typeDocument(TypeDocument.FACTURE)
                                  .dateCreationDocument(LocalDate.now())
                                  .dateModificationDocument(LocalDate.now())
                                  .vehiculeId(vehicule2.getId())
                                  .build();
            Document d3 = Document.builder()
                                  .nomDocument("devis sans titre")
                                  .emetteurDuDocument("garage toto")
                                  .emetteurDuDocument("jean jean")
                                  .typeDocument(TypeDocument.DEVIS)
                                  .dateCreationDocument(LocalDate.now())
                                  .dateModificationDocument(LocalDate.now())
                                  .vehiculeId(vehicule3.getId())
                                  .build();
            Document d4 = Document.builder()
                                  .nomDocument("devis jojo")
                                  .emetteurDuDocument("garage toto")
                                  .emetteurDuDocument("jojo")
                                  .typeDocument(TypeDocument.DEVIS)
                                  .dateCreationDocument(LocalDate.now())
                                  .dateModificationDocument(LocalDate.now())
                                  .vehiculeId(vehicule1.getId())
                                  .build();
            documentRepository.save(d1);
            documentRepository.save(d2);
            documentRepository.save(d3);
            documentRepository.save(d4);
            
        };
    }
}
