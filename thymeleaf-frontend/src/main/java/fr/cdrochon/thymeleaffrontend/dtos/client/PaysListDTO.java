package fr.cdrochon.thymeleaffrontend.dtos.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
public class PaysListDTO {
    
    public List<String> getPaysList() {
        return Arrays.asList(
                "AFGHANISTAN", "ALBANIE", "ALGERIE", "ANDORRE", "ANGOLA",
                "ANTIGUA_ET_BARBUDA", "ARABIE_SAOUDITE", "ARGENTINE", "ARMENIE",
                "AUSTRALIE", "AUTRICHE", "AZERBAIDJAN", "BAHAMAS", "BAHREIN",
                "BANGLADESH", "BARBADE", "BELGIQUE", "BELIZE", "BENIN",
                "BHOUTAN", "BIELORUSSIE", "BIRMANIE", "BOLIVIE",
                "BOSNIE_HERZEGOVINE", "BOTSWANA", "BRESIL", "BRUNEI",
                "BULGARIE", "BURKINA_FASO", "BURUNDI", "CABO_VERDE",
                "CAMBODGE", "CAMEROUN", "CANADA", "CENTRAFRIQUE", "CHILI",
                "CHINE", "CHYPRE", "COLOMBIE", "COMORES", "CONGO",
                "COREE_DU_NORD", "COREE_DU_SUD", "COSTA_RICA", "COTE_DIVOIRE",
                "CROATIE", "CUBA", "DANEMARK", "DJIBOUTI", "DOMINIQUE",
                "EGYPTE", "EMIRATS_ARABES_UNIS", "EQUATEUR", "ERYTHREE",
                "ESPAGNE", "ESTONIE", "ESWATINI", "ETATS_UNIS", "ETHIOPIE",
                "FIDJI", "FINLANDE", "FRANCE", "GABON", "GAMBIE", "GEORGIE",
                "GHANA", "GRECE", "GRENADE", "GUATEMALA", "GUINEE",
                "GUINEE_BISSAU", "GUYANA", "HAITI", "HONDURAS", "HONGRIE",
                "INDE", "INDONESIE", "IRAN", "IRAK", "IRLANDE", "ISLANDE",
                "ISRAEL", "ITALIE", "JAMAIQUE", "JAPON", "JORDANIE",
                "KAZAKHSTAN", "KENYA", "KIRGHIZISTAN", "KIRIBATI", "KOWEIT",
                "LAOS", "LESOTHO", "LETTONIE", "LIBAN", "LIBERIA", "LIBYE",
                "LIECHTENSTEIN", "LITUANIE", "LUXEMBOURG", "MACEDOINE",
                "MADAGASCAR", "MALAWI", "MALAISIE", "MALDIVES", "MALI",
                "MALTE", "MAROC", "MARSHALL", "MAURICE", "MAURITANIE",
                "MEXIQUE", "MICRONESIE", "MOLDAVIE", "MONACO", "MONGOLIE",
                "MONTENEGRO", "MOZAMBIQUE", "NAMIBIE", "NAURU", "NEPAL",
                "NICARAGUA", "NIGER", "NIGERIA", "NORVEGE", "NOUVELLE_ZELANDE",
                "OMAN", "OUGANDA", "OUZBEKISTAN", "PAKISTAN", "PALAOS",
                "PALESTINE", "PANAMA", "PAPOUASIE_NOUVELLE_GUINEE", "PARAGUAY",
                "PAYS_BAS", "PEROU", "PHILIPPINES", "POLOGNE", "PORTUGAL",
                "QATAR", "REPUBLIQUE_DOMINICAINE", "REPUBLIQUE_TCHEQUE",
                "ROUMANIE", "ROYAUME_UNI", "RUSSIE", "RWANDA", "SAINT_KITTS_ET_NEVIS",
                "SAINT_MARIN", "SAINT_VINCENT_ET_LES_GRENADINES", "SAINTE_LUCIE",
                "SALVADOR", "SAMOA", "SAO_TOME_ET_PRINCIPE", "SENEGAL",
                "SERBIE", "SEYCHELLES", "SIERRA_LEONE", "SINGAPOUR", "SLOVAQUIE",
                "SLOVENIE", "SOMALIE", "SOUDAN", "SOUDAN_DU_SUD", "SRI_LANKA",
                "SUEDE", "SUISSE", "SURINAME", "SYRIE", "TADJIKISTAN",
                "TANZANIE", "TCHAD", "THAILANDE", "TIMOR_ORIENTAL", "TOGO",
                "TONGA", "TRINITE_ET_TOBAGO", "TUNISIE", "TURKMENISTAN",
                "TURQUIE", "TUVALU", "UKRAINE", "URUGUAY", "VANUATU", "VATICAN",
                "VENEZUELA", "VIETNAM", "YEMEN", "ZAMBIE", "ZIMBABWE");
    }
}
