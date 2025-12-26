package com.openclassrooms.safetynetalerts.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
 * - Repeatable: Résultats déterministes avec date fixe de référence
 * - Self-Validating: Assertions claires
 * - Timely: Tests pour chaque méthode utilitaire
 */
@ExtendWith(MockitoExtension.class)
class UtilsUT {

    // Date fixe de référence pour garantir la répétabilité des tests
    // NOTE: Cette date doit correspondre à la date actuelle car Utils.calculateAge() utilise LocalDate.now()
    // Idéalement, Utils devrait accepter un Clock injectable pour une meilleure testabilité
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 12, 26);

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

    @Test
    void calculateAge_validMedicalRecord_returnsCorrectAge() {
        // Arrange
        int referenceYear = REFERENCE_DATE.getYear(); // 2024
        int birthYear = 2000;
        int expectedAge = referenceYear - birthYear; // 24 ans

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

    @Test
    void calculateAge_noMedicalRecord_returnsMinusOne() {
        // Arrange
        when(medicalRecordRepository.findByFirstNameAndLastName("John", "Doe"))
                .thenReturn(Optional.empty());

        // Act
        int actualAge = utils.calculateAge(person);

        // Assert
        assertEquals(-1, actualAge, "L'âge devrait être -1 quand aucun dossier médical n'existe");
        verify(medicalRecordRepository, times(1)).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    void isChild_age18_returnsTrue() {
        // Arrange
        String birthdate = "01/01/" + (REFERENCE_DATE.getYear() - 18); // Né en 2006, a 18 ans en 2024
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

    @Test
    void isChild_age19_returnsFalse() {
        // Arrange
        String birthdate = "01/01/" + (REFERENCE_DATE.getYear() - 19); // Né en 2005, a 19 ans en 2024
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
}
