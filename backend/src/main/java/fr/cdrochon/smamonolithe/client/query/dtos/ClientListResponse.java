package fr.cdrochon.smamonolithe.client.query.dtos;

import java.util.List;

/**
 * Wrapper de réponse pour les listes de clients.
 * Utilisé par Axon pour éviter les ambiguïtés de conversion sur List directes.
 */
public class ClientListResponse {
    private final List<ClientQueryDTO> items;

    public ClientListResponse(List<ClientQueryDTO> items) {
        this.items = items != null ? items : List.of();
    }

    public List<ClientQueryDTO> getItems() {
        return items;
    }
}

