package fr.cdrochon.smamonolithe.garage.entity;


import javax.persistence.Embeddable;

@Embeddable
public class ResponsableGarage {
    private String nomDuResponsable;
    private String prenomDuResponsable;
    private String mailDuResponsable;
    private String telDuResponsable;
}
