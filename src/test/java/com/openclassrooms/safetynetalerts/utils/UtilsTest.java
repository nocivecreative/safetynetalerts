package com.openclassrooms.safetynetalerts.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;

/**
 * Tests unitaires pour la classe Utils
 *
 * Principe FIRST:
 * - Fast: Tests rapides avec mocks
 * - Independent: Chaque test est autonome
 * - Repeatable: Résultats déterministes
 * - Self-Validating: Assertions claires
 * - Timely: Tests pour chaque méthode utilitaire
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - Utils")
class UtilsTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private Utils utils;

    private Person person;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
    }

    /**
     * Test 1.1: Vérifier que l'âge est calculé correctement
     *
     * Arrange: Créer un dossier médical avec une date de naissance connue
     * Act: Appeler calculateAge()
     * Assert: Vérifier que l'âge calculé est correct
     */
    @Test
    @DisplayName("calculateAge avec dossier médical valide doit retourner l'âge correct")
    void testCalculateAge_WithValidMedicalRecord_ShouldReturnCorrectAge() {
        // Arrange
        int currentYear = LocalDate.now().getYear();
        int birthYear = 2000;
        int expectedAge = currentYear - birthYear;

        String birthdate = "01/01/" + birthYear;
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate(birthdate);

        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.of(medicalRecord));

        // Act
        int actualAge = utils.calculateAge(person);

        // Assert
        assertEquals(expectedAge, actualAge, "L'âge calculé devrait être " + expectedAge);
        verify(medicalRecordRepository, times(1)).findByFirstNameAndLastName("John", "Doe");
    }

    /**
     * Test 1.2: Vérifier que -1 est retourné quand aucun dossier médical n'existe
     *
     * Arrange: Mock du repository pour retourner Optional.empty()
     * Act: Appeler calculateAge()
     * Assert: Vérifier que -1 est retourné
     */
    @Test
    @DisplayName("calculateAge sans dossier médical doit retourner -1")
    void testCalculateAge_WithNoMedicalRecord_ShouldReturnMinusOne() {
        // Arrange
        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.empty());

        // Act
        int actualAge = utils.calculateAge(person);

        // Assert
        assertEquals(-1, actualAge, "L'âge devrait être -1 quand aucun dossier médical n'existe");
        verify(medicalRecordRepository, times(1)).findByFirstNameAndLastName("John", "Doe");
    }

    /**
     * Test 1.3: Vérifier qu'une personne de 18 ans est considérée comme enfant
     *
     * Arrange: Mock calculateAge pour retourner 18
     * Act: Appeler isChild()
     * Assert: Vérifier que le résultat est true
     */
    @Test
    @DisplayName("isChild avec âge 18 ans doit retourner true")
    void testIsChild_WithAge18_ShouldReturnTrue() {
        // Arrange
        String birthdate = "01/01/" + (LocalDate.now().getYear() - 18);
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate(birthdate);

        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.of(medicalRecord));

        // Act
        boolean isChild = utils.isChild(person);

        // Assert
        assertTrue(isChild, "Une personne de 18 ans devrait être considérée comme enfant");
    }

    /**
     * Test 1.4: Vérifier qu'une personne de 19 ans n'est pas un enfant
     *
     * Arrange: Mock calculateAge pour retourner 19
     * Act: Appeler isChild()
     * Assert: Vérifier que le résultat est false
     */
    @Test
    @DisplayName("isChild avec âge 19 ans doit retourner false")
    void testIsChild_WithAge19_ShouldReturnFalse() {
        // Arrange
        String birthdate = "01/01/" + (LocalDate.now().getYear() - 19);
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate(birthdate);

        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.of(medicalRecord));

        // Act
        boolean isChild = utils.isChild(person);

        // Assert
        assertFalse(isChild, "Une personne de 19 ans ne devrait pas être considérée comme enfant");
    }

    /**
     * Test 1.5: Vérifier qu'une personne de 19 ans est adulte
     *
     * Arrange: Mock calculateAge pour retourner 19
     * Act: Appeler isAdult()
     * Assert: Vérifier que le résultat est true
     */
    @Test
    @DisplayName("isAdult avec âge 19 ans doit retourner true")
    void testIsAdult_WithAge19_ShouldReturnTrue() {
        // Arrange
        String birthdate = "01/01/" + (LocalDate.now().getYear() - 19);
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate(birthdate);

        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.of(medicalRecord));

        // Act
        boolean isAdult = utils.isAdult(person);

        // Assert
        assertTrue(isAdult, "Une personne de 19 ans devrait être considérée comme adulte");
    }

    /**
     * Test 1.6: Vérifier qu'une personne de 18 ans n'est pas adulte
     *
     * Arrange: Mock calculateAge pour retourner 18
     * Act: Appeler isAdult()
     * Assert: Vérifier que le résultat est false
     */
    @Test
    @DisplayName("isAdult avec âge 18 ans doit retourner false")
    void testIsAdult_WithAge18_ShouldReturnFalse() {
        // Arrange
        String birthdate = "01/01/" + (LocalDate.now().getYear() - 18);
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate(birthdate);

        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.of(medicalRecord));

        // Act
        boolean isAdult = utils.isAdult(person);

        // Assert
        assertFalse(isAdult, "Une personne de 18 ans ne devrait pas être considérée comme adulte");
    }
}
