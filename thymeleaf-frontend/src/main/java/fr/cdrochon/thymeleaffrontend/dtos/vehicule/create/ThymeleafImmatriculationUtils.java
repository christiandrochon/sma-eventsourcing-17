package fr.cdrochon.thymeleaffrontend.dtos.vehicule.create;

public class ThymeleafImmatriculationUtils {
    /**
     * Retourne la classe CSS à appliquer en fonction des erreurs de validation.
     *
     * @param hasFieldError              type de champ en erreur
     * @param immatriculationExisteError erreur diverse d'immatriculation (doublon, mauvais format, etc.)
     * @return String
     */
    public static String getValidationCssClass(boolean hasFieldError, boolean immatriculationExisteError) {
        if(hasFieldError || immatriculationExisteError) {
            return "validation-champ-surimpression";
        }
        return "";
    }
}
