package fr.cdrochon.smamonolithe.dossier.query.dtos;

import java.util.List;

/**
 * Wrapper de réponse pour les listes de dossiers.
 * Utilisé par Axon pour éviter les ambiguïtés de conversion sur List directes.
 */
public class DossierListResponse {
    private final List<DossierQueryDTO> items;

    public DossierListResponse(List<DossierQueryDTO> items) {
        this.items = items != null ? items : List.of();
    }

    public List<DossierQueryDTO> getItems() {
        return items;
    }
}

