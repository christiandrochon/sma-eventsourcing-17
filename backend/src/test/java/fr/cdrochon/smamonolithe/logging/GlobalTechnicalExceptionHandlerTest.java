package fr.cdrochon.smamonolithe.logging;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class GlobalTechnicalExceptionHandlerTest {

    @Test
    void shouldReturnInternalServerErrorPayload() {
        GlobalTechnicalExceptionHandler handler = new GlobalTechnicalExceptionHandler();
        ServerHttpRequest request = MockServerHttpRequest.get("/queries/vehicules/veh-1").build();

        ResponseEntity<Map<String, Object>> response = handler.handleUnhandledException(new RuntimeException("boom"), request);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> {
                    assertNotNull(response.getBody());
                    assertEquals(500, response.getBody().get("status"));
                },
                () -> {
                    assertNotNull(response.getBody());
                    assertEquals("Internal Server Error", response.getBody().get("error"));
                },
                () -> {
                    assertNotNull(response.getBody());
                    assertEquals("Erreur technique interne", response.getBody().get("message"));
                },
                () -> {
                    assertNotNull(response.getBody());
                    assertEquals("/queries/vehicules/veh-1", response.getBody().get("path"));
                }
        );
    }
}

