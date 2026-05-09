package fr.cdrochon.thymeleaffrontend.exception;

import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.class)
    public String handleHttpServerErrorException(HttpServerErrorException exception,
                                                 HttpServletRequest request,
                                                 Model model) {
        FrontendLoggers.error().error(
                "UI_TECH_EXCEPTION type={} method={} path={} status={} message={}",
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getStatusCode().value(),
                exception.getMessage(),
                exception
        );
        fillErrorModel(model, "Erreur serveur interne", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(WebClientResponseException.class)
    public String handleWebClientResponseException(WebClientResponseException exception,
                                                   HttpServletRequest request,
                                                   Model model) {
        FrontendLoggers.error().error(
                "UI_TECH_EXCEPTION type={} method={} path={} status={} message={}",
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getStatusCode().value(),
                exception.getMessage(),
                exception
        );
        fillErrorModel(model, "Erreur de communication avec le backend", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnhandledException(Exception exception,
                                           HttpServletRequest request,
                                           Model model) {
        FrontendLoggers.error().error(
                "UI_TECH_EXCEPTION type={} method={} path={} status={} message={}",
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage(),
                exception
        );
        fillErrorModel(model, "Erreur technique interne", request.getRequestURI());
        return "error";
    }

    private void fillErrorModel(Model model, String message, String fallbackRedirect) {
        model.addAttribute("alertClass", "alert-danger");
        model.addAttribute("errorMessage", message);
        model.addAttribute("urlRedirection", fallbackRedirect != null ? fallbackRedirect : "/");
    }
}
