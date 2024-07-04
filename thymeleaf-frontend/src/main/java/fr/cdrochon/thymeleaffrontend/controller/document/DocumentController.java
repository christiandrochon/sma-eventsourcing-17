package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.entity.document.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;
@Controller
public class DocumentController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    @GetMapping("/document/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getDocById(@PathVariable String id, Model model) {
        Document document = restClient.get().uri("/queries/documents/" + id)
                                    //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                    .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("document", document);
        return "document/view";
    }
    
    @GetMapping("/documents")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getAllDocuments(Model model) {
        List<Document> documents =
                restClient.get()
                          .uri("/queries/documents")
                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        model.addAttribute("documents", documents);
        return "document/documents";
    }
}
