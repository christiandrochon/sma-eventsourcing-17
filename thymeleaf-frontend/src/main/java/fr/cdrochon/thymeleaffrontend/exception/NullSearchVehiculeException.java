package fr.cdrochon.thymeleaffrontend.exception;

import lombok.Getter;

@Getter
public class NullSearchVehiculeException extends RuntimeException {
//    private final String errorMessage;
    private final String alertClass;
    private final String urlRedirection;
    

    public NullSearchVehiculeException(String errorMessage,  String alertClass, String urlRedirection) {
        super(errorMessage);
//        this.errorMessage = errorMessage;
        this.alertClass = alertClass;
        this.urlRedirection = urlRedirection;
    }
}
