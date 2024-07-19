package fr.cdrochon.smamonolithe;

//import fr.cdrochon.smamonolithe.garagesansevent.repository.GarageRepository;

import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SmaMonolitheApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SmaMonolitheApplication.class, args);
    }
    
    
    // Returns a Configurer instance with default components configured.
    // `AxonServerEventStore` is configured as Event Store by default.
    //    Configurer configurer = DefaultConfigurer.defaultConfiguration();
    
    
//    @Bean
        //    CommandLineRunner commandLineRunner(GarageRepository garageRepository,
        //                                        ClientRepository clientRepository,
        //                                        VehiculeRepository vehiculeRepository,
        //                                        DocumentRepository documentRepository) {
    CommandLineRunner commandLineRunner(GarageRepository garageRepository) {
        return args -> {
            
//            AdresseGarage adresse3 = AdresseGarage.builder()
//                                                  .rue("chemin de st jacques")
//                                                  .numeroDeRue("498")
//                                                  .cp("12500")
//                                                  .ville("Conques")
//                                                  .build();
//            AdresseGarage adresse4 = AdresseGarage.builder().rue("avenue du general brun").cp("48170").numeroDeRue("5").ville(
//                    "le malzieu").build();
//
//            //            List<Garage> garageList = List.of(
//            Garage garage1 = Garage.builder()
//                                   .nomGarage("Garage du Cres")
//                                   .mailResponsable("garageDuCres@gmail.com")
//                                   .adresseGarage(adresse3)
//                                   .build();
//            Garage garage2 = Garage.builder()
//                                   .nomGarage("Garage du louga")
//                                   .mailResponsable("garageLouge@toto.net")
//                                   .adresseGarage(adresse4)
//                                   .build();
//            //                                             );
//            //            garageRepository.saveAll(garageList);
//            garageRepository.save(garage1);
//            garageRepository.save(garage2);
            
            
            //            AdresseClient adresse1 = AdresseClient.builder()
            //                                                  .rue("rue du truc")
            //                                                  .numeroDeRue("17")
            //                                                  .cp("33300")
            //                                                  .ville("St Jean")
            //                                                  .build();
            //            AdresseClient adresse2 = AdresseClient.builder()
            //                                                  .rue("avenue du jo")
            //                                                  .cp("59140")
            //                                                  .numeroDeRue("51")
            //                                                  .ville(
            //                                              "Cloerwak")
            //                                                  .build();
            //
            //            Client client1 = Client.builder()
            //                                   .id(1L)
            //                                   .nomClient("Dubourg")
            //                                   .prenomClient("Jean")
            //                                   .mailClient("jean.dubourg@gmail.com")
            //                                   .telClient("0123456789")
            //                                   .adresseClient(adresse1)
            //                                   .build();
            //            Client client2 = Client.builder()
            //                                   .id(2L)
            //                                   .nomClient("Berga")
            //                                   .prenomClient("Christine")
            //                                   .mailClient("chirstine.berga@tut.io")
            //                                   .telClient("9876541320")
            //                                   .adresseClient(adresse2)
            //                                   .build();
            //            Client client3 = Client.builder()
            //                                   .id(3L)
            //                                   .nomClient("Poloua")
            //                                   .prenomClient("Gerard")
            //                                   .mailClient("gegelembrouille@tut.io")
            //                                   .telClient("8888888888")
            //                                   .adresseClient(adresse2)
            //                                   .build();
            //            clientRepository.save(client1);
            //            clientRepository.save(client2);
            //            clientRepository.save(client3);
            //
            //
            //
            //            Vehicule vehicule1 = Vehicule.builder()
            //                                         .immatriculationVehicule("PD-158-DZ")
            //                                         .dateMiseEnCirculationVehicule(LocalDate.now())
            //                                         .climatisationVehicule(true)
            //                                         .typeVehicule(TypeVehicule.VOITURE)
            //                                         .marqueVehicule(MarqueVehicule.BMW)
            ////                                         .clientId(client2.getId())
            //                                         .build();
            //            Vehicule vehicule2 = Vehicule.builder()
            //                                         .immatriculationVehicule("OS-184-PS")
            //                                         .dateMiseEnCirculationVehicule(LocalDate.of(2020, 12, 14))
            //                                         .climatisationVehicule(false)
            //                                         .typeVehicule(TypeVehicule.MOTO)
            //                                         .marqueVehicule(MarqueVehicule.HARLEY_DAVIDSON)
            //                                         .clientId(client3.getId())
            //                                         .build();
            //            Vehicule vehicule3 = Vehicule.builder()
            //                                         .immatriculationVehicule("SS-147-DG")
            //                                         .dateMiseEnCirculationVehicule(LocalDate.of(2023, 8, 1))
            //                                         .climatisationVehicule(true)
            //                                         .typeVehicule(TypeVehicule.VOITURE)
            //                                         .marqueVehicule(MarqueVehicule.AUDI)
            //                                         .clientId(client1.getId())
            //                                         .build();
            //            vehiculeRepository.save(vehicule1);
            //            vehiculeRepository.save(vehicule2);
            //            vehiculeRepository.save(vehicule3);
            //
            //
            //            Document d1 = Document.builder()
            //                                  .nomDocument("devis-00012547")
            //                                  .titreDocument("rdv pour essieu")
            //                                  .emetteurDuDocument("garage durand")
            //                                  .typeDocument(TypeDocument.DEVIS)
            //                                  .dateCreationDocument(LocalDate.now())
            //                                  .dateModificationDocument(LocalDate.now())
            //                                  //.vehiculeId(2L)
            //                                  .vehiculeId(vehicule1.getId())
            //                                  .build();
            //            Document d2 = Document.builder()
            //                                  .nomDocument("facture mr X")
            //                                  .titreDocument("facture du garage robert")
            //                                  .emetteurDuDocument("garage robert")
            //                                  .typeDocument(TypeDocument.FACTURE)
            //                                  .dateCreationDocument(LocalDate.now())
            //                                  .dateModificationDocument(LocalDate.now())
            ////                                  .vehiculeId(vehicule2.getId())
            //                                  .build();
            //            Document d3 = Document.builder()
            //                                  .nomDocument("devis sans titre")
            //                                  .emetteurDuDocument("garage toto")
            //                                  .emetteurDuDocument("jean jean")
            //                                  .typeDocument(TypeDocument.DEVIS)
            //                                  .dateCreationDocument(LocalDate.now())
            //                                  .dateModificationDocument(LocalDate.now())
            //                                  .vehiculeId(vehicule3.getId())
            //                                  .build();
            //            Document d4 = Document.builder()
            //                                  .nomDocument("devis jojo")
            //                                  .emetteurDuDocument("garage toto")
            //                                  .emetteurDuDocument("jojo")
            //                                  .typeDocument(TypeDocument.DEVIS)
            //                                  .dateCreationDocument(LocalDate.now())
            //                                  .dateModificationDocument(LocalDate.now())
            //                                  .vehiculeId(vehicule1.getId())
            //                                  .build();
            //            documentRepository.save(d1);
            //            documentRepository.save(d2);
            //            documentRepository.save(d3);
            //            documentRepository.save(d4);
            
        };
    }
}
