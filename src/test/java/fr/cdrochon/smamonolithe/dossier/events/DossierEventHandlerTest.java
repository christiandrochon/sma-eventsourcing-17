package fr.cdrochon.smamonolithe.dossier.events;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.sampleDossierCreatedEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DossierEventHandlerTest {

    @Mock
    private DossierCommandService dossierCommandService;

    @Test
    void shouldConvertCreatedEventAndCompleteCommandFuture() {
        DossierCreatedEvent event = sampleDossierCreatedEvent();
        DossierEventHandler handler = new DossierEventHandler(dossierCommandService);

        handler.on(event);

        ArgumentCaptor<DossierCommandDTO> captor = ArgumentCaptor.forClass(DossierCommandDTO.class);
        verify(dossierCommandService).completeDossierCreation(captor.capture());
        DossierCommandDTO dto = captor.getValue();

        assertEquals(event.getId(), dto.getId());
        assertEquals(event.getNomDossier(), dto.getNomDossier());
        assertEquals(event.getClient().getId(), dto.getClient().getId());
        assertEquals(event.getVehicule().getId(), dto.getVehicule().getId());
        assertEquals(event.getDossierStatus(), dto.getDossierStatus());
    }
}

