package com.openclassrooms.safetynetalerts.controller;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openclassrooms.safetynetalerts.dto.PersonDTO;
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

import com.openclassrooms.safetynetalerts.mapper.PersonMapper;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

/**
 * Tests d'intégration pour PersonController
 *
 * Tests critiques pour les endpoints CRUD et de consultation
 */
@WebMvcTest(PersonController.class)
class PersonControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private FirestationService firestationService;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    @MockitoBean
    private Utils utils;

    @MockitoBean
    private PersonMapper personMapper;

    private Person person1;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        person1 = new Person();
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setAddress("123 Main St");
        person1.setCity("Paris");
        person1.setZip("75001");
        person1.setPhone("111-111-1111");
        person1.setEmail("john.doe@email.com");

        medicalRecord = new MedicalRecord();
        medicalRecord.setFirstName("John");
        medicalRecord.setLastName("Doe");
        medicalRecord.setBirthdate("01/01/1990");
        medicalRecord.setMedications(Arrays.asList("aspirin:100mg"));
        medicalRecord.setAllergies(Arrays.asList("peanuts"));

        // Mock PersonMapper to return DTOs
        when(personMapper.toEntity(any(PersonDTO.class))).thenAnswer(invocation -> {
            PersonDTO dto = invocation.getArgument(0);
            Person p = new Person();
            p.setFirstName(dto.getFirstName());
            p.setLastName(dto.getLastName());
            p.setAddress(dto.getAddress());
            p.setCity(dto.getCity());
            p.setZip(dto.getZip());
            p.setPhone(dto.getPhone());
            p.setEmail(dto.getEmail());
            return p;
        });

        when(personMapper.toDto(any(Person.class))).thenAnswer(invocation -> {
            Person p = invocation.getArgument(0);
            return new PersonDTO(p.getFirstName(), p.getLastName(), p.getAddress(),
                    p.getCity(), p.getZip(), p.getPhone(), p.getEmail());
        });
    }

    // ==================== Tests GET /personInfo ====================

    @Test
    void getPersonsByLastName_existingLastName_returnsPersonsWithMedicalInfo() throws Exception {
        // Arrange
        when(personService.getPersonsByLastName("Doe")).thenReturn(Arrays.asList(person1));
        when(medicalRecordService.getMedicalRecord("John", "Doe")).thenReturn(Optional.of(medicalRecord));
        when(utils.calculateAge(person1)).thenReturn(35);

        // Act & Assert
        mockMvc.perform(get("/personInfo")
                .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persons").isArray())
                .andExpect(jsonPath("$.persons[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.persons[0].age").value(35))
                .andExpect(jsonPath("$.persons[0].medicalHistory.medications[0]").value("aspirin:100mg"));

        verify(personService, times(1)).getPersonsByLastName("Doe");
    }

    // ==================== Tests GET /fire ====================
    @Test
    void getPersonsByAddress_fire_returnsResidentsWithMedicalInfoAndStation() throws Exception {
        // Arrange
        when(personService.getPersonsByAddress("123 Main St")).thenReturn(Arrays.asList(person1));
        when(firestationService.getStationNumberByAddress("123 Main St")).thenReturn(1);
        when(medicalRecordService.getMedicalRecord("John", "Doe")).thenReturn(Optional.of(medicalRecord));
        when(utils.calculateAge(person1)).thenReturn(35);

        // Act & Assert
        mockMvc.perform(get("/fire")
                .param("address", "123 Main St"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firestationNumber").value(1))
                .andExpect(jsonPath("$.personList[0].LastName").value("Doe"))
                .andExpect(jsonPath("$.personList[0].age").value(35));

        verify(personService, times(1)).getPersonsByAddress("123 Main St");
    }

    // ==================== Tests GET /communityEmail ====================

    @Test
    void getEmailsByCity_validCity_returnsUniqueEmails() throws Exception {
        // Arrange
        Set<String> emails = Set.of("john@email.com", "jane@email.com");
        when(personService.getEmailsByCity("Paris")).thenReturn(emails);

        // Act & Assert
        mockMvc.perform(get("/communityEmail")
                .param("city", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddresses.length()").value(2));

        verify(personService, times(1)).getEmailsByCity("Paris");
    }

    // ==================== Tests POST /person ====================

    @Test
    void addPerson_validData_returnsCreated() throws Exception {
        // Arrange
        String personJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "address": "123 Main St",
                    "city": "Paris",
                    "zip": "75001",
                    "phone": "111-111-1111",
                    "email": "john.doe@email.com"
                }
                """;

        doNothing().when(personService).addPerson(any(Person.class));

        // Act & Assert
        mockMvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(personService, times(1)).addPerson(any(Person.class));
    }

    @Test
    void addPerson_personAlreadyExists_returnsBadRequest() throws Exception {
        // Arrange
        String personJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "address": "123 Main St",
                    "city": "Paris",
                    "zip": "75001",
                    "phone": "111-111-1111",
                    "email": "john.doe@email.com"
                }
                """;

        doThrow(new IllegalArgumentException("Person already exist"))
                .when(personService).addPerson(any(Person.class));

        // Act & Assert
        mockMvc.perform(post("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests PUT /person ====================

    @Test
    void updatePerson_validData_returnsOk() throws Exception {
        // Arrange
        String personJson = """
                {
                    "address": "456 New St",
                    "city": "Lyon",
                    "zip": "69001",
                    "phone": "333-333-3333",
                    "email": "john.new@email.com"
                }
                """;

        doNothing().when(personService).updatePerson(any(Person.class));

        // Act & Assert
        mockMvc.perform(put("/person")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("456 New St"));

        verify(personService, times(1)).updatePerson(any(Person.class));
    }

    // ==================== Tests DELETE /person ====================

    @Test
    void deletePerson_personExists_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(personService).deletePerson("John", "Doe");

        // Act & Assert
        mockMvc.perform(delete("/person")
                .param("firstName", "John")
                .param("lastName", "Doe"))
                .andExpect(status().isNoContent());

        verify(personService, times(1)).deletePerson("John", "Doe");
    }

    @Test
    void deletePerson_personNotFound_returnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Person not found"))
                .when(personService).deletePerson("Unknown", "Person");

        // Act & Assert
        mockMvc.perform(delete("/person")
                .param("firstName", "Unknown")
                .param("lastName", "Person"))
                .andExpect(status().isBadRequest());
    }
}
