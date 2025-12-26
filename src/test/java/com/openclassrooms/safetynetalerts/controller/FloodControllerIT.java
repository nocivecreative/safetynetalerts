package com.openclassrooms.safetynetalerts.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

/**
 * Tests d'intégration pour FloodController
 *
 * Tests critiques pour l'endpoint flood/stations
 */
@WebMvcTest(FloodController.class)
class FloodControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirestationRepository firestationRepository;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    @MockitoBean
    private Utils utils;

    private Person person1;
    private Person person2;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        person1 = new Person();
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setAddress("123 Main St");
        person1.setPhone("111-111-1111");

        person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        person2.setAddress("456 Oak Ave");
        person2.setPhone("222-222-2222");

        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate("01/01/1990");
        medicalRecord.setMedications(Arrays.asList("aspirin:100mg"));
        medicalRecord.setAllergies(Arrays.asList("peanuts"));
    }

    // ==================== Tests GET /flood/stations ====================

    @Test
    void getPersonsByStations_multipleStations_returnsHouseholdsByAddress() throws Exception {
        // Arrange
        List<Integer> stations = Arrays.asList(1, 2);
        Set<String> addresses = Set.of("123 Main St", "456 Oak Ave");

        when(firestationRepository.findAddressesByStations(stations)).thenReturn(addresses);
        when(personService.getPersonsByAddress("123 Main St")).thenReturn(Arrays.asList(person1));
        when(personService.getPersonsByAddress("456 Oak Ave")).thenReturn(Arrays.asList(person2));
        when(medicalRecordService.getMedicalRecord("John", "Doe")).thenReturn(Optional.of(medicalRecord));
        when(medicalRecordService.getMedicalRecord("Jane", "Doe")).thenReturn(Optional.empty());
        when(utils.calculateAge(any(Person.class))).thenReturn(35);

        // Act & Assert
        mockMvc.perform(get("/flood/stations")
                .param("stations", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households").isArray())
                .andExpect(jsonPath("$.households.length()").value(2))
                .andExpect(jsonPath("$.households[0].address").exists())
                .andExpect(jsonPath("$.households[0].residents").isArray())
                .andExpect(jsonPath("$.households[0].residents[0].lastName").exists())
                .andExpect(jsonPath("$.households[0].residents[0].phoneNumber").exists())
                .andExpect(jsonPath("$.households[0].residents[0].age").exists());

        verify(firestationRepository, times(1)).findAddressesByStations(stations);
    }

    @Test
    void getPersonsByStations_noAddresses_returnsEmptyList() throws Exception {
        // Arrange
        List<Integer> stations = Arrays.asList(99);
        when(firestationRepository.findAddressesByStations(stations)).thenReturn(Collections.emptySet());

        // Act & Assert
        mockMvc.perform(get("/flood/stations")
                .param("stations", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.households").isEmpty());

        verify(firestationRepository, times(1)).findAddressesByStations(stations);
    }
}
