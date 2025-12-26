package com.openclassrooms.safetynetalerts.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.service.FirestationService;

/**
 * Tests d'intégration pour FirestationController
 *
 * Tests critiques pour les endpoints CRUD
 */
@WebMvcTest(FirestationController.class)
class FirestationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirestationService firestationService;

    // ==================== Tests POST /firestation ====================

    @Test
    void addFirestation_validData_returnsCreated() throws Exception {
        // Arrange
        String firestationJson = """
                {
                    "address": "123 Main St",
                    "station": 1
                }
                """;

        doNothing().when(firestationService).addMapping(any(Firestation.class));

        // Act & Assert
        mockMvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firestationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.station").value(1));

        verify(firestationService, times(1)).addMapping(any(Firestation.class));
    }

    @Test
    void addFirestation_addressAlreadyExists_returnsBadRequest() throws Exception {
        // Arrange
        String firestationJson = """
                {
                    "address": "123 Main St",
                    "station": 1
                }
                """;

        doThrow(new IllegalArgumentException("L'adresse existe déjà"))
                .when(firestationService).addMapping(any(Firestation.class));

        // Act & Assert
        mockMvc.perform(post("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firestationJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests PUT /firestation ====================

    @Test
    void updateFirestation_validData_returnsOk() throws Exception {
        // Arrange
        String firestationJson = """
                {
                    "address": "123 Main St",
                    "station": 2
                }
                """;

        doNothing().when(firestationService).updateMapping(any(Firestation.class));

        // Act & Assert
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firestationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.station").value(2));

        verify(firestationService, times(1)).updateMapping(any(Firestation.class));
    }

    @Test
    void updateFirestation_addressNotFound_returnsBadRequest() throws Exception {
        // Arrange
        String firestationJson = """
                {
                    "address": "999 Unknown St",
                    "station": 1
                }
                """;

        doThrow(new IllegalArgumentException("Adresse non trouvée"))
                .when(firestationService).updateMapping(any(Firestation.class));

        // Act & Assert
        mockMvc.perform(put("/firestation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firestationJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests DELETE /firestation ====================

    @Test
    void deleteFirestation_byAddress_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(firestationService).deleteMapping("123 Main St", null);

        // Act & Assert
        mockMvc.perform(delete("/firestation")
                .param("address", "123 Main St"))
                .andExpect(status().isNoContent());

        verify(firestationService, times(1)).deleteMapping("123 Main St", null);
    }

    @Test
    void deleteFirestation_byStation_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(firestationService).deleteMapping(null, 1);

        // Act & Assert
        mockMvc.perform(delete("/firestation")
                .param("station", "1"))
                .andExpect(status().isNoContent());

        verify(firestationService, times(1)).deleteMapping(null, 1);
    }

    @Test
    void deleteFirestation_bothParameters_returnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Spécifiez soit l'adresse soit le numéro, pas les deux"))
                .when(firestationService).deleteMapping("123 Main St", 1);

        // Act & Assert
        mockMvc.perform(delete("/firestation")
                .param("address", "123 Main St")
                .param("station", "1"))
                .andExpect(status().isBadRequest());
    }
}
