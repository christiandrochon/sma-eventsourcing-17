package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.dtos.AdresseCommandMapper;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleAdresseClient;
import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleAdresseDTO;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class AdresseCommandMapperTest {

    @Test
    void shouldConvertDTOToAdresseClient() {
        AdresseClient result = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(sampleAdresseDTO());
        assertNotNull(result);
        assertEquals("Rue de la Paix", result.getRue());
        assertEquals("75001", result.getCp());
        assertEquals("Paris", result.getVille());
        assertEquals(Pays.FRANCE, result.getPays());
    }

    @Test
    void shouldConvertAdresseClientToDTO() {
        ClientAdresseDTO result = AdresseCommandMapper.convertAdresseToClientAdresseDTO(sampleAdresseClient());
        assertNotNull(result);
        assertEquals("Rue de la Paix", result.getRue());
        assertEquals("75001", result.getCp());
        assertEquals("Paris", result.getVille());
        assertEquals(Pays.FRANCE, result.getPays());
    }

    @Test
    void shouldPreserveComplementAdresse() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        dto.setComplementAdresse("Bâtiment B");
        AdresseClient result = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(dto);
        assertEquals("Bâtiment B", result.getComplementAdresse());
    }

    @Test
    void shouldHandleNullComplementAdresse() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        dto.setComplementAdresse(null);
        AdresseClient result = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(dto);
        assertNull(result.getComplementAdresse());
    }

    @Test
    void shouldConvertAllPaysValues() {
        for (Pays pays : Pays.values()) {
            ClientAdresseDTO dto = sampleAdresseDTO();
            dto.setPays(pays);
            AdresseClient result = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(dto);
            assertEquals(pays, result.getPays());
        }
    }

    @Test
    void shouldPreserveNumeroDeRue() {
        AdresseClient result = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(sampleAdresseDTO());
        assertEquals("12", result.getNumeroDeRue());
    }

    @Test
    void shouldReturnNewObjectOnEachConversion() {
        AdresseClient r1 = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(sampleAdresseDTO());
        AdresseClient r2 = AdresseCommandMapper.convertAdresseCommandDTOToAdresse(sampleAdresseDTO());
        assertNotSame(r1, r2);
    }
}