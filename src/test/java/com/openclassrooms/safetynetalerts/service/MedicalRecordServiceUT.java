package com.openclassrooms.safetynetalerts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    void createMedicalRecord_recordAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.createMedicalRecord(medicalRecord)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    // ==================== Tests updateMedicalRecord ====================

    @Test
    void updateMedicalRecord_recordNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.update("Unknown", "Person", medicalRecord))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.updateMedicalRecord("Unknown", "Person", medicalRecord)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    // ==================== Tests deleteMedicalRecord ====================

    @Test
    void deleteMedicalRecord_recordNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(medicalRecordRepository.delete("Unknown", "Person")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.deleteMedicalRecord("Unknown", "Person")
        );

        assertTrue(exception.getMessage().contains("not found"));
    }
}
