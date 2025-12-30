package com.openclassrooms.safetynetalerts.service;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;

/**
 * Tests unitaires pour MedicalRecordService
 *
 * Tests critiques uniquement:
 * - Exceptions pour dossiers existants/inexistants
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceUT {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate("01/01/1990");
        medicalRecord.setMedications(Arrays.asList("aspirin:100mg"));
        medicalRecord.setAllergies(Arrays.asList("peanuts"));
    }

    // ==================== Tests createMedicalRecord ====================

    @Test
    void createMedicalRecord_withNewRecord_savesSuccessfully() {
        // Arrange
        when(medicalRecordRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(false);
        when(medicalRecordRepository.save(medicalRecord)).thenReturn(medicalRecord);

        // Act
        MedicalRecord result = medicalRecordService.createMedicalRecord(medicalRecord);

        // Assert
        verify(medicalRecordRepository).existsByFirstNameAndLastName("John", "Doe");
        verify(medicalRecordRepository).save(medicalRecord);
        assertTrue(result != null);
    }

    @Test
    void createMedicalRecord_recordAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.createMedicalRecord(medicalRecord));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    // ==================== Tests updateMedicalRecord ====================

    @Test
    void updateMedicalRecord_recordNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.findByFirstNameAndLastName("Unknown", "Person"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.updateMedicalRecord("Unknown", "Person", medicalRecord));

        assertEquals("Medical record for Unknown Person not found", exception.getMessage());
    }

    // ==================== Tests deleteMedicalRecord ====================

    @Test
    void deleteMedicalRecord_recordNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.existsByFirstNameAndLastName("Unknown", "Person")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.deleteMedicalRecord("Unknown", "Person"));

        assertEquals("Medical record for Unknown Person not found", exception.getMessage());
    }
}
