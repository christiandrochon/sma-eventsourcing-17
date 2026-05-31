package fr.cdrochon.thymeleaffrontend.validation;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendEdgeCaseValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    /**
     * Execute une initialisation unique avant tous les tests de la classe.
     */
    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    /**
     * Execute un nettoyage unique apres tous les tests de la classe.
     */
    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    /**
     * Genere dynamiquement une serie de cas de test a partir de scenarios construits
     * a l'execution.
     */
    @TestFactory
    Stream<DynamicTest> shouldCatch100EdgeCases() {
        return IntStream.range(0, 100)
                .mapToObj(i -> DynamicTest.dynamicTest("edge-case-" + i, () -> {
                    Object dto = buildInvalidDto(i);

                    assertThat(validator.validate(dto))
                            .as("Case %s should be invalid", i)
                            .isNotEmpty();
                }));
    }

    private static Object buildInvalidDto(int i) {
        return switch (i % 10) {
            case 0 -> {
                ClientThymDTO dto = FrontendDtoFixtures.validClient(i);
                dto.setNomClient("");
                yield dto;
            }
            case 1 -> {
                ClientThymDTO dto = FrontendDtoFixtures.validClient(i);
                dto.setPrenomClient("A");
                yield dto;
            }
            case 2 -> {
                ClientThymDTO dto = FrontendDtoFixtures.validClient(i);
                dto.setMailClient("bad-mail");
                yield dto;
            }
            case 3 -> {
                ClientThymDTO dto = FrontendDtoFixtures.validClient(i);
                dto.setTelClient("123");
                yield dto;
            }
            case 4 -> {
                ClientThymDTO dto = FrontendDtoFixtures.validClient(i);
                dto.getAdresse().setCp("ABCD");
                yield dto;
            }
            case 5 -> {
                DocumentThymDTO dto = FrontendDtoFixtures.validDocument(i);
                dto.setNomDocument("ab");
                yield dto;
            }
            case 6 -> {
                DocumentThymDTO dto = FrontendDtoFixtures.validDocument(i);
                dto.setDateCreationDocument("");
                yield dto;
            }
            case 7 -> {
                VehiculeThymDTO dto = FrontendDtoFixtures.validVehicule(i);
                dto.setImmatriculationVehicule("AA123AA");
                yield dto;
            }
            case 8 -> {
                DossierThymDTO dto = FrontendDtoFixtures.validDossier(i);
                dto.setNomDossier("ab");
                yield dto;
            }
            default -> {
                GaragePostDTO dto = FrontendDtoFixtures.validGarage(i);
                dto.setMailResp("invalid");
                yield dto;
            }
        };
    }
}

