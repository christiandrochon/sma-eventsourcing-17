package fr.cdrochon.thymeleaffrontend.dtos.vehicule.create;

/**
 * Utility class referenced by Thymeleaf templates to style immatriculation field validation state.
 */
public final class ThymeleafImmatriculationUtils {

    private ThymeleafImmatriculationUtils() {
    }

    public static String getValidationCssClass(boolean hasFieldErrors, boolean hasBusinessError) {
        return (hasFieldErrors || hasBusinessError) ? "is-invalid" : "";
    }
}

