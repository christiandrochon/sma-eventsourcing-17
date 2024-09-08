package fr.cdrochon.thymeleaffrontend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(HttpServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Optional, as HttpServerErrorException already indicates a 5xx error
    public ResponseEntity<String> handleHttpServerErrorException(HttpServerErrorException e) {
        // Log the error, send a notification, etc.
        
        // Create a custom response body or use the exception's message
        String responseBody = "LOG ERREUR  - An error occurred on the server. Please try again later." + e.getMessage();
        
        // Return a ResponseEntity
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Prise en charge des erreurs globales en line avec l'auth keycloak
     *
     * @return redirection vers la page de login
     */
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public String handleAuthenticationException() {
        return "redirect:/login";  // Redirige vers la page de login
    }
}
