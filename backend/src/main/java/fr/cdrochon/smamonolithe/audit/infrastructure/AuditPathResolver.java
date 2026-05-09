package fr.cdrochon.smamonolithe.audit.infrastructure;

import fr.cdrochon.smamonolithe.audit.domain.AuditAction;
import fr.cdrochon.smamonolithe.audit.domain.AuditResource;

/**
 * Résout la ressource métier et l'action d'audit à partir du couple (méthode HTTP, chemin).
 *
 * <p>Exemples de mappings :</p>
 * <pre>
 *   GET  /queries/vehicules/abc-123  → READ   VEHICULE  id=abc-123
 *   GET  /queries/vehicules          → READ   VEHICULE  id=null
 *   POST /commands/createVehicule    → CREATE VEHICULE  id=null
 *   GET  /queries/clients/xyz        → READ   CLIENT    id=xyz
 * </pre>
 */
public final class AuditPathResolver {

    private AuditPathResolver() {}

    // -----------------------------------------------------------------------
    // Résolution de la ressource métier
    // -----------------------------------------------------------------------

    public static AuditResource resolveResource(String path) {
        if (path == null) return AuditResource.UNKNOWN;
        String p = path.toLowerCase();

        if (p.contains("vehicule"))  return AuditResource.VEHICULE;
        if (p.contains("client"))    return AuditResource.CLIENT;
        if (p.contains("garage"))    return AuditResource.GARAGE;
        if (p.contains("dossier"))   return AuditResource.DOSSIER;
        if (p.contains("document"))  return AuditResource.DOCUMENT;

        return AuditResource.UNKNOWN;
    }

    // -----------------------------------------------------------------------
    // Résolution de l'identifiant de la ressource
    // Extrait le dernier segment de chemin si c'est un identifiant (non vide, pas un mot-clé)
    // -----------------------------------------------------------------------

    private static final java.util.Set<String> KNOWN_KEYWORDS = java.util.Set.of(
            "queries", "commands", "vehicules", "clients", "garages", "dossiers", "documents",
            "createvehicule", "createclient", "creategarage", "createdossier", "createdocument",
            "actuator", "health", "info"
    );

    public static String resolveResourceId(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] segments = path.split("/");
        // Parcourir en sens inverse pour trouver le premier segment non-vide non-mot-clé
        for (int i = segments.length - 1; i >= 0; i--) {
            String seg = segments[i];
            if (!seg.isEmpty() && !KNOWN_KEYWORDS.contains(seg.toLowerCase())) {
                return seg;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Résolution de l'action à partir de la méthode HTTP
    // -----------------------------------------------------------------------

    public static AuditAction resolveAction(String httpMethod) {
        if (httpMethod == null) return AuditAction.READ;
        return switch (httpMethod.toUpperCase()) {
            case "POST"   -> AuditAction.CREATE;
            case "PUT",
                 "PATCH"  -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            default       -> AuditAction.READ;
        };
    }
}

