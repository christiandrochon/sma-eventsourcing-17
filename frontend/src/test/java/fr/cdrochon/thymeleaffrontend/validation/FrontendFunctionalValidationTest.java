package fr.cdrochon.thymeleaffrontend.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class FrontendFunctionalValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    Stream<DynamicTest> shouldPass100FunctionalValidationCases() {
        return IntStream.range(0, 100)
                .mapToObj(i -> DynamicTest.dynamicTest("functional-case-" + i, () -> {
                    Object dto = switch (i % 5) {
                        case 0 -> FrontendDtoFixtures.validClient(i);
                        case 1 -> FrontendDtoFixtures.validDocument(i);
                        case 2 -> FrontendDtoFixtures.validVehicule(i);
                        case 3 -> FrontendDtoFixtures.validDossier(i);
                        default -> FrontendDtoFixtures.validGarage(i);
                    };

                    assertThat(validator.validate(dto))
                            .as("Case %s should be valid", i)
                            .isEmpty();

                    /**
                     * Technical guard: serialization should stay stable for valid DTO payloads.
                     */
                    String json = OBJECT_MAPPER.writeValueAsString(dto);
                    assertThat(json).isNotBlank();
                }));
    }
}

