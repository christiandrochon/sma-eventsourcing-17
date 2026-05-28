package fr.cdrochon.thymeleaffrontend.integratuion;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class FrontendMvcIntegratuionTest {

    private static HttpServer backendStub;
    private static int backendPort;
    private static final Map<String, StubResponse> ROUTES = new ConcurrentHashMap<>();

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void startStubServer() throws IOException {
        backendStub = HttpServer.create(new InetSocketAddress(0), 0);
        backendPort = backendStub.getAddress().getPort();
        backendStub.createContext("/", FrontendMvcIntegratuionTest::handleRequest);
        backendStub.start();
    }

    @AfterAll
    static void stopStubServer() {
        backendStub.stop(0);
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("external.service.url", () -> "http://localhost:" + backendPort);
    }

    @BeforeEach
    void prepareDefaultRoutes() {
        ROUTES.clear();
        stubJson("/queries/clients", 200, "[" + clientJson("cli-1") + "]");
        stubJson("/queries/clients/cli-1", 200, clientJson("cli-1"));

        stubJson("/queries/documents", 200, "[" + documentJson("doc-1") + "]");
        stubJson("/queries/documents/doc-1", 200, documentJson("doc-1"));

        stubJson("/queries/dossiers", 200, "[" + dossierJson("dos-1") + "]");
        stubJson("/queries/dossiers/dos-1", 200, dossierJson("dos-1"));
    }

    @Test
    void shouldRenderCreateClientForm() throws Exception {
        mockMvc.perform(get("/createClient"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/createClientForm"))
                .andExpect(model().attributeExists("clientDTO"))
                .andExpect(model().attributeExists("clientStatuses"))
                .andExpect(model().attributeExists("paysList"));
    }

    @Test
    void shouldRenderCreateDocumentForm() throws Exception {
        mockMvc.perform(get("/createDocument"))
                .andExpect(status().isOk())
                .andExpect(view().name("document/createDocumentForm"))
                .andExpect(model().attributeExists("documentDTO"))
                .andExpect(model().attributeExists("typeDocuments"))
                .andExpect(model().attributeExists("documentStatuses"));
    }

    @Test
    void shouldRenderCreateDossierForm() throws Exception {
        asyncGet("/createDossier")
                .andExpect(status().isOk())
                .andExpect(view().name("dossier/createDossierForm"))
                .andExpect(model().attributeExists("dossierDTO"))
                .andExpect(model().attributeExists("dossierStatuses"))
                .andExpect(model().attributeExists("vehiculeStatuses"));
    }

    @Test
    void shouldRenderClientsList() throws Exception {
        asyncGet("/clients")
                .andExpect(status().isOk())
                .andExpect(view().name("client/clients"))
                .andExpect(model().attributeExists("clients"));
    }

    @Test
    void shouldRenderClientDetails() throws Exception {
        asyncGet("/client/cli-1")
                .andExpect(status().isOk())
                .andExpect(view().name("client/view"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    void shouldRenderDocumentsList() throws Exception {
        asyncGet("/documents")
                .andExpect(status().isOk())
                .andExpect(view().name("document/documents"))
                .andExpect(model().attributeExists("documents"));
    }

    @Test
    void shouldRenderDocumentDetails() throws Exception {
        asyncGet("/document/doc-1")
                .andExpect(status().isOk())
                .andExpect(view().name("document/view"))
                .andExpect(model().attributeExists("document"));
    }

    @Test
    void shouldRenderDossiersList() throws Exception {
        asyncGet("/dossiers")
                .andExpect(status().isOk())
                .andExpect(view().name("dossier/dossiers"))
                .andExpect(model().attributeExists("dossiers"));
    }

    @Test
    void shouldRenderDossierDetails() throws Exception {
        asyncGet("/dossier/dos-1")
                .andExpect(status().isOk())
                .andExpect(view().name("dossier/view"))
                .andExpect(model().attributeExists("dossier"));
    }

    @Test
    void shouldRedirectToErrorWhenClientsBackend404() throws Exception {
        stubJson("/queries/clients", 404, "{\"error\":\"not found\"}");

        asyncGet("/clients")
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToErrorWhenDocumentsBackend500() throws Exception {
        stubJson("/queries/documents", 500, "{\"error\":\"boom\"}");

        asyncGet("/documents")
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToErrorWhenDocumentByIdBackend400() throws Exception {
        stubJson("/queries/documents/doc-1", 400, "{\"error\":\"bad request\"}");

        asyncGet("/document/doc-1")
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToErrorWhenDossiersBackend400() throws Exception {
        stubJson("/queries/dossiers", 400, "{\"error\":\"bad request\"}");

        asyncGet("/dossiers")
                .andExpect(status().isOk());
    }

    private ResultActions asyncGet(String uri) throws Exception {
        MvcResult asyncResult = mockMvc.perform(get(uri))
                .andExpect(request().asyncStarted())
                .andReturn();
        return mockMvc.perform(asyncDispatch(asyncResult));
    }

    private static void stubJson(String path, int status, String body) {
        ROUTES.put(path, new StubResponse(status, "application/json", body));
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        StubResponse response = ROUTES.get(exchange.getRequestURI().getPath());
        if (response == null) {
            response = new StubResponse(404, "application/json", "{\"error\":\"route not stubbed\"}");
        }

        byte[] payload = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", response.contentType());
        exchange.sendResponseHeaders(response.status(), payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    private static String clientJson(String id) {
        return """
                {
                  "id":"%s",
                  "nomClient":"Durand",
                  "prenomClient":"Alice",
                  "mailClient":"alice@mail.com",
                  "telClient":"0601020304",
                  "adresse":{
                    "numeroDeRue":"10",
                    "rue":"Rue des Tests",
                    "complementAdresse":"Bat A",
                    "cp":"75001",
                    "ville":"Paris",
                    "pays":"FRANCE"
                  },
                  "clientStatus":"ACTIF",
                  "vehicule":{
                    "id":"veh-1",
                    "immatriculationVehicule":"AB-123-CD",
                    "dateMiseEnCirculationVehicule":"2021-01-01T00:00:00Z",
                    "vehiculeStatus":"EN_CIRCULATION"
                  }
                }
                """.formatted(id);
    }

    private static String documentJson(String id) {
        return """
                {
                  "id":"%s",
                  "nomDocument":"Facture test",
                  "titreDocument":"Titre test",
                  "emetteurDuDocument":"Garage Test",
                  "typeDocument":{"nomTypeDocument":"FACTURE"},
                  "dateCreationDocument":"2024-01-01T00:00:00Z",
                  "dateModificationDocument":"2024-01-02T00:00:00Z",
                  "documentStatus":"CREATED"
                }
                """.formatted(id);
    }

    private static String dossierJson(String id) {
        return """
                {
                  "id":"%s",
                  "nomDossier":"Dossier test",
                  "dateCreationDossier":"2024-01-01T00:00:00Z",
                  "dateModificationDossier":"2024-01-02T00:00:00Z",
                  "dossierStatus":"OUVERT",
                  "vehicule":{
                    "id":"veh-2",
                    "immatriculationVehicule":"EF-456-GH",
                    "dateMiseEnCirculationVehicule":"2020-06-01T00:00:00Z",
                    "vehiculeStatus":"EN_CIRCULATION"
                  },
                  "client":{
                    "id":"cli-2",
                    "nomClient":"Martin",
                    "prenomClient":"Paul",
                    "mailClient":"paul@mail.com",
                    "telClient":"0611223344",
                    "clientStatus":"ACTIF",
                    "adresse":{
                      "numeroDeRue":"12",
                      "rue":"Rue QA",
                      "complementAdresse":"",
                      "cp":"69001",
                      "ville":"Lyon",
                      "pays":"FRANCE"
                    }
                  }
                }
                """.formatted(id);
    }

    private record StubResponse(int status, String contentType, String body) {
    }
}

