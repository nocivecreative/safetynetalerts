package com.openclassrooms.safetynetalerts.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;

/**
 * Tests d'intégration pour MedicalRecordController
 *
 * Tests critiques pour les endpoints CRUD
 */
@WebMvcTest(MedicalRecordController.class)
class MedicalRecordControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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

    // ==================== Tests POST /medicalRecord ====================

    @Test
    void createMedicalRecord_validData_returnsCreated() throws Exception {
        // Arrange
        String medicalRecordJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthdate": "01/01/1990",
                    "medications": ["aspirin:100mg"],
                    "allergies": ["peanuts"]
                }
                """;

        when(medicalRecordService.createMedicalRecord(any(MedicalRecord.class)))
                .thenReturn(medicalRecord);

        // Act & Assert
        mockMvc.perform(post("/medicalRecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(medicalRecordJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.birthdate").value("01/01/1990"));

        verify(medicalRecordService, times(1)).createMedicalRecord(any(MedicalRecord.class));
    }

    @Test
    void createMedicalRecord_recordAlreadyExists_returnsBadRequest() throws Exception {
        // Arrange
        String medicalRecordJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthdate": "01/01/1990",
                    "medications": ["aspirin:100mg"],
                    "allergies": ["peanuts"]
                }
                """;

        when(medicalRecordService.createMedicalRecord(any(MedicalRecord.class)))
                .thenThrow(new IllegalArgumentException("Medical record already exists"));

        // Act & Assert
        mockMvc.perform(post("/medicalRecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(medicalRecordJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests PUT /medicalRecord ====================

    @Test
    void updateMedicalRecord_validData_returnsOk() throws Exception {
        // Arrange
        String medicalRecordJson = """
                {
                    "birthdate": "02/02/1990",
                    "medications": ["newMed:500mg"],
                    "allergies": ["newAllergy"]
                }
                """;

        when(medicalRecordService.updateMedicalRecord(eq("John"), eq("Doe"), any(MedicalRecord.class)))
                .thenReturn(medicalRecord);

        // Act & Assert
        mockMvc.perform(put("/medicalRecord")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(medicalRecordJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(medicalRecordService, times(1)).updateMedicalRecord(eq("John"), eq("Doe"), any(MedicalRecord.class));
    }

    @Test
    void updateMedicalRecord_recordNotFound_returnsBadRequest() throws Exception {
        // Arrange
        String medicalRecordJson = """
                {
                    "birthdate": "01/01/2000",
                    "medications": [],
                    "allergies": []
                }
                """;

        when(medicalRecordService.updateMedicalRecord(eq("Unknown"), eq("Person"), any(MedicalRecord.class)))
                .thenThrow(new IllegalArgumentException("Medical record not found"));

        // Act & Assert
        mockMvc.perform(put("/medicalRecord")
                .param("firstName", "Unknown")
                .param("lastName", "Person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(medicalRecordJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests DELETE /medicalRecord ====================

    @Test
    void deleteMedicalRecord_recordExists_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(medicalRecordService).deleteMedicalRecord("John", "Doe");

        // Act & Assert
        mockMvc.perform(delete("/medicalRecord")
                .param("firstName", "John")
                .param("lastName", "Doe"))
                .andExpect(status().isNoContent());

        verify(medicalRecordService, times(1)).deleteMedicalRecord("John", "Doe");
    }

    @Test
    void deleteMedicalRecord_recordNotFound_returnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Medical record not found"))
                .when(medicalRecordService).deleteMedicalRecord("Unknown", "Person");

        // Act & Assert
        mockMvc.perform(delete("/medicalRecord")
                .param("firstName", "Unknown")
                .param("lastName", "Person"))
                .andExpect(status().isBadRequest());
    }
}
