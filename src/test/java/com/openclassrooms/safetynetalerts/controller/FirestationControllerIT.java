package com.openclassrooms.safetynetalerts.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationDTO;
import com.openclassrooms.safetynetalerts.mapper.FirestationMapper;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.utils.Utils;

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

    @MockitoBean
    private FirestationMapper firestationMapper;

    @MockitoBean
    private Utils utils;

    private Person child;
    private Person adult;

    private Firestation firestation;
    private FirestationDTO firestationDTO;

    @BeforeEach
    void setUp() {
        child = new Person();
        child.setFirstName("Emma");
        child.setLastName("Boyd");
        child.setAddress("123 Main St");
        child.setPhone("111-111-1111");

        adult = new Person();
        adult.setFirstName("John");
        adult.setLastName("Boyd");
        adult.setAddress("123 Main St");
        adult.setPhone("222-222-2222");
    }

    // ==================== Tests GET /firestation ====================

    @Test
    void getPersonsByStation_validStation_returnsOkWithCorrectData() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(child, adult, adult);
        when(firestationService.getPersonsCoveredByStation(1)).thenReturn(persons);
        when(utils.isChild(child)).thenReturn(true);
        when(utils.isChild(adult)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/firestation")
                .param("stationNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residents").isArray())
                .andExpect(jsonPath("$.residents.length()").value(3))
                .andExpect(jsonPath("$.adultCount").value(2))
                .andExpect(jsonPath("$.childCount").value(1))
                .andExpect(jsonPath("$.residents[0].firstName").value("Emma"))
                .andExpect(jsonPath("$.residents[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.residents[0].address").value("123 Main St"))
                .andExpect(jsonPath("$.residents[0].phone").value("111-111-1111"));

        verify(firestationService, times(1)).getPersonsCoveredByStation(1);
        verify(utils, times(3)).isChild(any(Person.class));
    }

    @Test
    void getPersonsByStation_noPersons_returnsEmptyList() throws Exception {
        // Arrange
        when(firestationService.getPersonsCoveredByStation(99)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/firestation")
                .param("stationNumber", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residents").isEmpty())
                .andExpect(jsonPath("$.adultCount").value(0))
                .andExpect(jsonPath("$.childCount").value(0));

        verify(firestationService, times(1)).getPersonsCoveredByStation(99);
    }

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

        firestation = new Firestation();
        firestation.setAddress("123 Main St");
        firestation.setStation(1);

        firestationDTO = new FirestationDTO("123 Main St", 1);

        when(firestationMapper.toEntity(any(FirestationDTO.class)))
                .thenReturn(firestation);
        when(firestationService.addMapping(any(Firestation.class)))
                .thenReturn(firestation);
        when(firestationMapper.toDto(any(Firestation.class)))
                .thenReturn(firestationDTO);

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

        firestation = new Firestation();
        firestation.setAddress("123 Main St");
        firestation.setStation(1);

        when(firestationMapper.toEntity(any(FirestationDTO.class)))
                .thenReturn(firestation);
        when(firestationService.addMapping(any(Firestation.class)))
                .thenThrow(new IllegalArgumentException("L'adresse existe déjà"));

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
                    "station": 99
                }
                """;

        Firestation inputFirestation = new Firestation();
        inputFirestation.setStation(99);

        Firestation updatedFirestation = new Firestation();
        updatedFirestation.setAddress("123 Main St");
        updatedFirestation.setStation(99);

        FirestationDTO updatedDTO = new FirestationDTO("123 Main St", 99);

        when(firestationMapper.toEntity(any(FirestationDTO.class)))
                .thenReturn(inputFirestation);
        when(firestationService.updateMapping(eq("123 Main St"), any(Firestation.class)))
                .thenReturn(updatedFirestation);
        when(firestationMapper.toDto(updatedFirestation))
                .thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/firestation")
                .param("address", "123 Main St")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firestationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.station").value(99));

        verify(firestationService, times(1)).updateMapping(anyString(), any(Firestation.class));
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
                .when(firestationService).updateMapping(anyString(), any(Firestation.class));

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
