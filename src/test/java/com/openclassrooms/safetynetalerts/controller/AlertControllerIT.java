package com.openclassrooms.safetynetalerts.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

/**
 * Tests d'intégration pour AlertController avec @WebMvcTest
 *
 * Tests des endpoints:
 * - GET /firestation?stationNumber=<num>
 * - GET /phoneAlert?firestation=<num>
 * - GET /childAlert?address=<address>
 */
@WebMvcTest(AlertController.class)
class AlertControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirestationService firestationService;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private Utils utils;

    private Person child;
    private Person adult;

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

    // ==================== Tests GET /phoneAlert ====================

    @Test
    void getPhoneByStation_validStation_returnsUniquePhones() throws Exception {
        // Arrange
        Set<String> phones = Set.of("111-111-1111", "222-222-2222");
        when(firestationService.getPhoneNumbersByStation(1)).thenReturn(phones);

        // Act & Assert
        mockMvc.perform(get("/phoneAlert")
                .param("firestation", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumbersList").isArray())
                .andExpect(jsonPath("$.phoneNumbersList.length()").value(2));

        verify(firestationService, times(1)).getPhoneNumbersByStation(1);
    }

    // ==================== Tests GET /childAlert ====================

    @Test
    void getChildrenByAddress_withChildren_returnsChildrenWithHousehold() throws Exception {
        // Arrange
        List<Person> personsAtAddress = Arrays.asList(child, adult);
        when(personService.getPersonsByAddress("123 Main St")).thenReturn(personsAtAddress);
        when(utils.isChild(child)).thenReturn(true);
        when(utils.isChild(adult)).thenReturn(false);
        when(utils.calculateAge(child)).thenReturn(10);

        // Act & Assert
        mockMvc.perform(get("/childAlert")
                .param("address", "123 Main St"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children.length()").value(1))
                .andExpect(jsonPath("$.children[0].firstName").value("Emma"))
                .andExpect(jsonPath("$.children[0].lastName").value("Boyd"))
                .andExpect(jsonPath("$.children[0].age").value(10))
                .andExpect(jsonPath("$.children[0].otherHouseholdMembers").isArray())
                .andExpect(jsonPath("$.children[0].otherHouseholdMembers.length()").value(1))
                .andExpect(jsonPath("$.children[0].otherHouseholdMembers[0].firstName").value("John"))
                .andExpect(jsonPath("$.children[0].otherHouseholdMembers[0].lastName").value("Boyd"));

        verify(personService, times(1)).getPersonsByAddress("123 Main St");
        verify(utils, times(2)).isChild(any(Person.class));
        verify(utils, times(1)).calculateAge(child);
    }

    @Test
    void getChildrenByAddress_noChildren_returnsEmptyList() throws Exception {
        // Arrange
        List<Person> personsAtAddress = Arrays.asList(adult);
        when(personService.getPersonsByAddress("456 Oak Ave")).thenReturn(personsAtAddress);
        when(utils.isChild(adult)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/childAlert")
                .param("address", "456 Oak Ave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.children").isEmpty());

        verify(personService, times(1)).getPersonsByAddress("456 Oak Ave");
        verify(utils, times(1)).isChild(adult);
    }
}
