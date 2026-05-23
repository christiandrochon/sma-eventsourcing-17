package fr.cdrochon.smamonolithe.client.query.entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut métier du client")
public enum ClientStatus {
    ACTIF, HISTORISE, INACTIF
}
