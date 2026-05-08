package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleAdresseClient;
import static org.junit.jupiter.api.Assertions.*;

class AdresseQueryMapperTest {

    @Test
    void shouldReturnNullWhenAdresseClientIsNull() {
        assertNull(AdresseQueryMapper.convertAdresseToClientAdresseDTO(null));
    }

    @Test
    void shouldMapAllFieldsCorrectly() {
        ClientAdresseDTO dto = AdresseQueryMapper.convertAdresseToClientAdresseDTO(sampleAdresseClient());
        assertNotNull(dto);
        assertEquals("Rue de la Paix", dto.getRue());
        assertEquals("75001", dto.getCp());
        assertEquals("Paris", dto.getVille());
        assertEquals(Pays.FRANCE, dto.getPays());
        assertEquals("12", dto.getNumeroDeRue());
    }

    @Test
    void shouldNotMapComplementAdresse() {
        // AdresseQueryMapper ne mappe pas complementAdresse → doit être null
        AdresseClient adresse = sampleAdresseClient();
        adresse.setComplementAdresse("Bâtiment C");
        ClientAdresseDTO dto = AdresseQueryMapper.convertAdresseToClientAdresseDTO(adresse);
        assertNull(dto.getComplementAdresse()); // confirmé par lecture du code
    }

    @Test
    void shouldHandleNullPays() {
        AdresseClient adresse = sampleAdresseClient();
        adresse.setPays(null);
        ClientAdresseDTO dto = AdresseQueryMapper.convertAdresseToClientAdresseDTO(adresse);
        assertNull(dto.getPays());
    }

    @Test
    void shouldReturnNewObjectEachCall() {
        ClientAdresseDTO d1 = AdresseQueryMapper.convertAdresseToClientAdresseDTO(sampleAdresseClient());
        ClientAdresseDTO d2 = AdresseQueryMapper.convertAdresseToClientAdresseDTO(sampleAdresseClient());
        assertNotSame(d1, d2);
    }
}