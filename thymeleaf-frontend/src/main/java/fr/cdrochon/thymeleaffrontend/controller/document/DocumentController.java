package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertPostDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;
@Controller
public class DocumentController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final RestClient restClient;
    public DocumentController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    @GetMapping("/document/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getDocById(@PathVariable String id, Model model) {
        DocumentConvertPostDTO document = restClient.get().uri(externalServiceUrl + "/queries/documents/" + id)
                                                    //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                                    .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("document", document);
        return "document/view";
    }
    
    @GetMapping("/documents")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getAllDocuments(Model model) {
        List<DocumentConvertPostDTO> documents =
                restClient.get()
                          .uri(externalServiceUrl + "/queries/documents")
                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        model.addAttribute("documents", documents);
        return "document/documents";
    }
}
