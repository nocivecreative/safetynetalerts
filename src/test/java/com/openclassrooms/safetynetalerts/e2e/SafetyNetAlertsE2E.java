package com.openclassrooms.safetynetalerts.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.client.RestTemplate;

import com.openclassrooms.safetynetalerts.dto.PersonDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResidentDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationDTO;
import com.openclassrooms.safetynetalerts.dto.medicalrecord.MedicalRecordDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonMedicalProfileDTO;

/**
 * Tests End-to-End pour Safety Net Alerts
 *
 * Ce test valide un workflow utilisateur complet de bout en bout :
 * 1. Création d'une nouvelle personne
 * 2. Création de son dossier médical
 * 3. Mapping à une firestation
 * 4. Vérification via différents endpoints (personInfo, firestation, fire,
 * childAlert)
 * 5. Test de dégradation (suppression dossier médical → âge = -1)
 * 6. Nettoyage complet dans l'ordre logique
 *
 * Utilise @SpringBootTest pour charger le contexte Spring complet (vrais
 * services)
 * et @DirtiesContext pour garantir l'isolation (recharge contexte après le
 * test).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SafetyNetAlertsE2E {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    // Test data constants
    private static final String TEST_FIRST_NAME = "PrénomTest";
    private static final String TEST_LAST_NAME = "NomTest";
    private static final String TEST_ADDRESS = "999 Test Street";
    private static final String TEST_CITY = "TestCity";
    private static final String TEST_ZIP = "99999";
    private static final String TEST_PHONE = "555-9999";
    private static final String TEST_EMAIL = "alice.martin@test.com";
    private static final String TEST_BIRTHDATE = "01/15/2010";
    private static final int TEST_STATION = 99;
    private static final int EXPECTED_AGE = 15; // Âge calculé pour la date de naissance 01/15/2010 en 2025

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("Complete E2E workflow: Create person with medical record, verify via multiple endpoints, test degradation, cleanup")
    void completeUserWorkflow_createVerifyAndCleanup_shouldSucceed() {
        // PHASE 1: CREATE
        createPerson();
        createMedicalRecord();
        createFirestation();

        // PHASE 2: VERIFY INITIAL STATE
        verifyPersonInfo();
        verifyFirestationCoverage();
        verifyFireDetails();
        verifyChildAlert();

        // PHASE 3: UPDATE MEDICAL RECORD
        updateMedicalRecord();
        verifyUpdatedMedicalRecord();

        // PHASE 4: DEGRADATION TEST (medical record deletion)
        deleteMedicalRecord();
        verifyPersonWithoutMedicalRecord();

        // PHASE 5: CLEANUP
        deleteFirestation();
        verifyFirestationRemoved();
        deletePerson();
        verifyPersonRemoved();
    }

    // ========== PHASE 1: CREATE ==========

    private void createPerson() {
        PersonDTO newPerson = new PersonDTO(
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_ADDRESS,
                TEST_CITY,
                TEST_ZIP,
                TEST_PHONE,
                TEST_EMAIL);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                getBaseUrl() + "/person",
                newPerson,
                Void.class);

        assertThat(response.getStatusCode())
                .as("POST /person should return 201 Created")
                .isEqualTo(HttpStatus.CREATED);
    }

    private void createMedicalRecord() {
        MedicalRecordDTO newRecord = new MedicalRecordDTO(
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_BIRTHDATE,
                Arrays.asList("testadicine:100mg"),
                Arrays.asList("testallergy"));

        ResponseEntity<Void> response = restTemplate.postForEntity(
                getBaseUrl() + "/medicalRecord",
                newRecord,
                Void.class);

        assertThat(response.getStatusCode())
                .as("POST /medicalRecord should return 201 Created")
                .isEqualTo(HttpStatus.CREATED);
    }

    private void createFirestation() {
        FirestationDTO newMapping = new FirestationDTO(TEST_ADDRESS, TEST_STATION);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                getBaseUrl() + "/firestation",
                newMapping,
                Void.class);

        assertThat(response.getStatusCode())
                .as("POST /firestation should return 201 Created")
                .isEqualTo(HttpStatus.CREATED);
    }

    // ========== PHASE 2: VERIFY INITIAL STATE ==========

    private void verifyPersonInfo() {
        String url = getBaseUrl() + "/personInfolastName?lastName=" + TEST_LAST_NAME;
        ResponseEntity<PersonInfoResponseDTO> response = restTemplate.getForEntity(
                url,
                PersonInfoResponseDTO.class);

        assertThat(response.getStatusCode())
                .as("GET /personInfolastName should return 200 OK")
                .isEqualTo(HttpStatus.OK);

        PersonInfoResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getPersons()).isNotEmpty();

        // Find Alice Martin in the list (note: PersonMedicalProfileDTO doesn't have
        // firstName)
        PersonMedicalProfileDTO alice = body.getPersons().stream()
                .filter(p -> p.getLastName().equals(TEST_LAST_NAME))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice Martin not found in person info response"));

        // Verify basic person information
        assertThat(alice.getLastName()).isEqualTo(TEST_LAST_NAME);
        assertThat(alice.getAddress()).isEqualTo(TEST_ADDRESS);
        assertThat(alice.getEmail()).isEqualTo(TEST_EMAIL);

        // Verify age calculation (fixed value for repeatability)
        assertThat(alice.getAge())
                .as("Age should be calculated from birthdate " + TEST_BIRTHDATE)
                .isEqualTo(EXPECTED_AGE);

        // Verify medical record (nested in MedicalHistoryDTO)
        assertThat(alice.getMedicalHistory()).isNotNull();
        assertThat(alice.getMedicalHistory().getMedications())
                .as("Medications should contain testadicine:100mg")
                .containsExactly("testadicine:100mg");
        assertThat(alice.getMedicalHistory().getAllergies())
                .as("Allergies should contain testallergy")
                .containsExactly("testallergy");
    }

    private void verifyFirestationCoverage() {
        String url = getBaseUrl() + "/firestation?stationNumber=" + TEST_STATION;
        ResponseEntity<FirestationCoverageResponseDTO> response = restTemplate.getForEntity(
                url,
                FirestationCoverageResponseDTO.class);

        assertThat(response.getStatusCode())
                .as("GET /firestation should return 200 OK")
                .isEqualTo(HttpStatus.OK);

        FirestationCoverageResponseDTO coverage = response.getBody();
        assertThat(coverage).isNotNull();

        // Alice is a child (~15 years old), so childCount should be at least 1
        assertThat(coverage.getChildCount())
                .as("Alice is a child, childCount should be >= 1")
                .isGreaterThanOrEqualTo(1);

        // Verify Alice is in the residents list
        assertThat(coverage.getResidents())
                .as("Residents should contain Alice Martin")
                .extracting("firstName", "lastName", "address", "phone")
                .contains(tuple(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_ADDRESS, TEST_PHONE));
    }

    private void verifyFireDetails() {
        String url = getBaseUrl() + "/fire?address=" + TEST_ADDRESS;
        ResponseEntity<FireAddressResponseDTO> response = restTemplate.getForEntity(
                url,
                FireAddressResponseDTO.class);

        assertThat(response.getStatusCode())
                .as("GET /fire should return 200 OK")
                .isEqualTo(HttpStatus.OK);

        FireAddressResponseDTO fireInfo = response.getBody();
        assertThat(fireInfo).isNotNull();

        // Verify station number
        assertThat(fireInfo.getFirestationNumber())
                .as("Firestation number should be " + TEST_STATION)
                .isEqualTo(TEST_STATION);

        // Verify Alice is in the person list (note: FireAddressResidentDTO has LastName
        // with capital L)
        assertThat(fireInfo.getPersonList())
                .as("Person list should contain Alice Martin")
                .extracting("lastName")
                .contains(TEST_LAST_NAME);

        // Find Alice and verify her medical details
        FireAddressResidentDTO alice = fireInfo.getPersonList().stream()
                .filter(p -> p.getLastName().equals(TEST_LAST_NAME))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice Martin not found in fire address response"));

        // Verify age (fixed value for repeatability)
        assertThat(alice.getAge()).isEqualTo(EXPECTED_AGE);

        // Verify medical info (nested in MedicalHistoryDTO)
        assertThat(alice.getMedicalHistory()).isNotNull();
        assertThat(alice.getMedicalHistory().getMedications()).containsExactly("testadicine:100mg");
        assertThat(alice.getMedicalHistory().getAllergies()).containsExactly("testallergy");
    }

    private void verifyChildAlert() {
        String url = getBaseUrl() + "/childAlert?address=" + TEST_ADDRESS;
        ResponseEntity<ChildAlertResponseDTO> response = restTemplate.getForEntity(
                url,
                ChildAlertResponseDTO.class);

        assertThat(response.getStatusCode())
                .as("GET /childAlert should return 200 OK")
                .isEqualTo(HttpStatus.OK);

        ChildAlertResponseDTO childAlert = response.getBody();
        assertThat(childAlert).isNotNull();

        // Verify Alice is in the children list
        assertThat(childAlert.getChildren())
                .as("Children list should contain Alice Martin")
                .extracting("firstName", "lastName")
                .contains(tuple(TEST_FIRST_NAME, TEST_LAST_NAME));

        // Verify Alice's age in child alert
        ChildInfoDTO alice = childAlert.getChildren().stream()
                .filter(c -> c.getFirstName().equals(TEST_FIRST_NAME))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice Martin not found in child alert response"));

        // Verify age (fixed value for repeatability)
        assertThat(alice.getAge()).isEqualTo(EXPECTED_AGE);
    }

    // ========== PHASE 3: UPDATE MEDICAL RECORD ==========

    private void updateMedicalRecord() {
        MedicalRecordDTO updatedRecord = new MedicalRecordDTO(
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_BIRTHDATE, // Keep same birthdate
                Arrays.asList("updatedMed1:200mg", "updatedMed2:50mg"), // New medications
                Arrays.asList("updatedAllergy1", "updatedAllergy2") // New allergies
        );

        String url = getBaseUrl() + "/medicalRecord?firstName=" + TEST_FIRST_NAME +
                "&lastName=" + TEST_LAST_NAME;

        restTemplate.put(url, updatedRecord);
    }

    private void verifyUpdatedMedicalRecord() {
        String url = getBaseUrl() + "/personInfolastName?lastName=" + TEST_LAST_NAME;
        ResponseEntity<PersonInfoResponseDTO> response = restTemplate.getForEntity(
                url,
                PersonInfoResponseDTO.class);

        assertThat(response.getStatusCode())
                .as("GET /personInfolastName should return 200 OK after update")
                .isEqualTo(HttpStatus.OK);

        PersonInfoResponseDTO body = response.getBody();
        assertThat(body).isNotNull();

        PersonMedicalProfileDTO alice = body.getPersons().stream()
                .filter(p -> p.getLastName().equals(TEST_LAST_NAME))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Person not found after medical record update"));

        // Verify updated medications
        assertThat(alice.getMedicalHistory()).isNotNull();
        assertThat(alice.getMedicalHistory().getMedications())
                .as("Medications should be updated")
                .containsExactly("updatedMed1:200mg", "updatedMed2:50mg");

        // Verify updated allergies
        assertThat(alice.getMedicalHistory().getAllergies())
                .as("Allergies should be updated")
                .containsExactly("updatedAllergy1", "updatedAllergy2");

        // Verify age is still correct after update (fixed value for repeatability)
        assertThat(alice.getAge())
                .as("Age should still be correct after update")
                .isEqualTo(EXPECTED_AGE);
    }

    // ========== PHASE 4: DEGRADATION TEST ==========

    private void deleteMedicalRecord() {
        String url = getBaseUrl() + "/medicalRecord?firstName=" + TEST_FIRST_NAME +
                "&lastName=" + TEST_LAST_NAME;
        restTemplate.delete(url);
    }

    private void verifyPersonWithoutMedicalRecord() {
        String url = getBaseUrl() + "/personInfolastName?lastName=" + TEST_LAST_NAME;
        ResponseEntity<PersonInfoResponseDTO> response = restTemplate.getForEntity(
                url,
                PersonInfoResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PersonInfoResponseDTO body = response.getBody();
        assertThat(body).isNotNull();

        // Find Alice Martin in the list
        PersonMedicalProfileDTO alice = body.getPersons().stream()
                .filter(p -> p.getLastName().equals(TEST_LAST_NAME))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice Martin should still exist after medical record deletion"));

        // Verify age is -1 when medical record is missing
        assertThat(alice.getAge())
                .as("Age should be -1 when medical record is deleted")
                .isEqualTo(-1);
    }

    // ========== PHASE 5: CLEANUP ==========

    private void deleteFirestation() {
        String url = getBaseUrl() + "/firestation?address=" + TEST_ADDRESS;
        restTemplate.delete(url);
    }

    private void verifyFirestationRemoved() {
        String url = getBaseUrl() + "/firestation?stationNumber=" + TEST_STATION;
        ResponseEntity<FirestationCoverageResponseDTO> response = restTemplate.getForEntity(
                url,
                FirestationCoverageResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        FirestationCoverageResponseDTO coverage = response.getBody();
        assertThat(coverage).isNotNull();

        // Alice should no longer be in residents after firestation mapping is deleted
        assertThat(coverage.getResidents())
                .as("Residents should not contain Alice Martin after firestation deletion")
                .extracting("firstName", "lastName")
                .doesNotContain(tuple(TEST_FIRST_NAME, TEST_LAST_NAME));
    }

    private void deletePerson() {
        String url = getBaseUrl() + "/person?firstName=" + TEST_FIRST_NAME +
                "&lastName=" + TEST_LAST_NAME;
        restTemplate.delete(url);
    }

    private void verifyPersonRemoved() {
        String url = getBaseUrl() + "/personInfolastName?lastName=" + TEST_LAST_NAME;
        ResponseEntity<PersonInfoResponseDTO> response = restTemplate.getForEntity(
                url,
                PersonInfoResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PersonInfoResponseDTO body = response.getBody();
        assertThat(body).isNotNull();

        // Alice should no longer be in the list
        assertThat(body.getPersons())
                .as("Person list should not contain Alice after deletion")
                .filteredOn(p -> p.getLastName().equals(TEST_LAST_NAME))
                .isEmpty();
    }
}
