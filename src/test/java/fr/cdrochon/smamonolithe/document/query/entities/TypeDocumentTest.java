package fr.cdrochon.smamonolithe.document.query.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeDocumentTest {

    @Test
    void shouldExposePredefinedDevisValue() {
        assertEquals("DEVIS", TypeDocument.DEVIS.getNomTypeDocument());
    }

    @Test
    void shouldExposePredefinedFactureValue() {
        assertEquals("FACTURE", TypeDocument.FACTURE.getNomTypeDocument());
    }

    @Test
    void shouldContainBothPredefinedValues() {
        assertTrue(TypeDocument.PREDEFINED_VALUES.contains(TypeDocument.DEVIS));
        assertTrue(TypeDocument.PREDEFINED_VALUES.contains(TypeDocument.FACTURE));
        assertEquals(2, TypeDocument.PREDEFINED_VALUES.size());
    }

    @Test
    void shouldBeUnmodifiablePredefinedValuesCollection() {
        assertThrows(UnsupportedOperationException.class,
                () -> TypeDocument.PREDEFINED_VALUES.add(new TypeDocument("OTHER")));
    }

    @Test
    void shouldBuildCustomTypeWithBuilder() {
        TypeDocument custom = TypeDocument.builder().nomTypeDocument("BON_DE_COMMANDE").build();

        assertNotNull(custom);
        assertEquals("BON_DE_COMMANDE", custom.getNomTypeDocument());
    }
}
