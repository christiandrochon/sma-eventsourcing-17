package fr.cdrochon.smamonolithe.garage.controller;


import fr.cdrochon.smamonolithe.garage.entity.Garage;
import fr.cdrochon.smamonolithe.garage.repository.GarageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GarageRestController {
    
    private final GarageRepository garageRepository;
    
    public GarageRestController(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }
//    @GetMapping("/garage/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public Garage garageById(@PathVariable Long id){
        return garageRepository.findById(id).get();
    }
    
//    @GetMapping("/garages")
//    @PreAuthorize("hasAuthority('USER')")
    public List<Garage> garageSet(){
        return garageRepository.findAll();
    }
}
