package fr.cdrochon.thymeleaffrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThymeleafRestController {
    
    /**
     * Par defaut, l'appli s'ouvre sans path. Lorsque c'est le cas, on renseigne le path à une page index.html
     *
     * @return page index.html
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

}
