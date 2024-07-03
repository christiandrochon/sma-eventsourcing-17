package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.client.entity.Client;
import fr.cdrochon.smamonolithe.client.service.ClientService;
import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.repository.VehiculeRepository;
import fr.cdrochon.smamonolithe.vehicule.services.VehiculeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/client")
public class ViewVehiculeController {

    VehiculeRepository vehiculeRepository;
    @Autowired
    VehiculeService vehiculeService;
    @Autowired
    ClientService clientService;

    public ViewVehiculeController(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    @GetMapping("/view")
    public ModelAndView view(Long id) {
//        Optional<Vehicule> vehicule = vehiculeService.findItem(id);
        Client client = clientService.findItem(id);
        Optional<Vehicule> vehicule = vehiculeService.findItem(client.getId());
        return new ModelAndView("/vehicule/view", "vehicule", vehicule);
    }
}
