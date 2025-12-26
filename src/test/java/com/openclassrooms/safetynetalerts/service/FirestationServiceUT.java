package com.openclassrooms.safetynetalerts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

/**
 * Tests unitaires pour FirestationService
 *
 * Tests critiques uniquement:
 * - Validation des mappings (exceptions)
 * - Gestion des suppressions (address OU station, pas les deux)
 */
@ExtendWith(MockitoExtension.class)
class FirestationServiceUT {

    @Mock
    private FirestationRepository firestationRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private FirestationService firestationService;

    private Firestation firestation;
    private Person person1;
    private Person person2;
    private Person person3;

    @BeforeEach
    void setUp() {
        firestation = new Firestation();
        firestation.setAddress("123 Main St");
        firestation.setStation(1);

        person1 = new Person();
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setAddress("123 Main St");
        person1.setPhone("123-456-7890");

        person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Smith");
        person2.setAddress("456 Oak Ave");
        person2.setPhone("456-789-0123");

        person3 = new Person();
        person3.setFirstName("Bob");
        person3.setLastName("Johnson");
        person3.setAddress("123 Main St");
        person3.setPhone("789-012-3456");
    }

    // ==================== Tests addMapping ====================

    @Test
    void addMapping_addressAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(firestationRepository.existsByAddress("123 Main St")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> firestationService.addMapping(firestation)
        );

        assertEquals("L'adresse existe déjà", exception.getMessage());
        verify(firestationRepository, never()).addFirestation(any());
    }

    @Test
    void addMapping_stationNumberAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(firestationRepository.existsByAddress("123 Main St")).thenReturn(false);
        when(firestationRepository.existsByStation(1)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> firestationService.addMapping(firestation)
        );

        assertEquals("Le numéro de la caserne existe déjà", exception.getMessage());
        verify(firestationRepository, never()).addFirestation(any());
    }

    // ==================== Tests updateMapping ====================

    @Test
    void updateMapping_addressNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(firestationRepository.existsByAddress("123 Main St")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> firestationService.updateMapping(firestation)
        );

        assertEquals("Adresse non trouvée", exception.getMessage());
        verify(firestationRepository, never()).updateFirestation(any());
    }

    // ==================== Tests deleteMapping ====================

    @Test
    void deleteMapping_bothParametersProvided_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> firestationService.deleteMapping("123 Main St", 1)
        );

        assertEquals("Spécifiez soit l'adresse soit le numéro, pas les deux", exception.getMessage());
        verify(firestationRepository, never()).deleteFirestationByAddress(anyString());
        verify(firestationRepository, never()).deleteFirestationByStation(anyInt());
    }

    @Test
    void deleteMapping_neitherParameterProvided_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> firestationService.deleteMapping(null, null)
        );

        assertEquals("L'adresse ou le numéro doivent être fournis", exception.getMessage());
        verify(firestationRepository, never()).deleteFirestationByAddress(anyString());
        verify(firestationRepository, never()).deleteFirestationByStation(anyInt());
    }

    // ==================== Tests getPhoneNumbersByStation ====================

    @Test
    void getPhoneNumbersByStation_withMultiplePersons_returnsUniqueSortedPhoneNumbers() {
        // Arrange
        int stationNumber = 1;
        List<String> addresses = Arrays.asList("123 Main St", "456 Oak Ave");

        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(addresses);
        when(personRepository.findByAddress("123 Main St")).thenReturn(Arrays.asList(person1, person3));
        when(personRepository.findByAddress("456 Oak Ave")).thenReturn(Arrays.asList(person2));

        // Act
        Set<String> phoneNumbers = firestationService.getPhoneNumbersByStation(stationNumber);

        // Assert
        assertNotNull(phoneNumbers);
        assertEquals(3, phoneNumbers.size());
        assertTrue(phoneNumbers.contains("123-456-7890"));
        assertTrue(phoneNumbers.contains("456-789-0123"));
        assertTrue(phoneNumbers.contains("789-012-3456"));

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
        verify(personRepository, times(1)).findByAddress("123 Main St");
        verify(personRepository, times(1)).findByAddress("456 Oak Ave");
    }

    @Test
    void getPhoneNumbersByStation_withNoAddresses_returnsEmptySet() {
        // Arrange
        int stationNumber = 99;
        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(Collections.emptyList());

        // Act
        Set<String> phoneNumbers = firestationService.getPhoneNumbersByStation(stationNumber);

        // Assert
        assertNotNull(phoneNumbers);
        assertTrue(phoneNumbers.isEmpty());

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
        verify(personRepository, never()).findByAddress(anyString());
    }

    @Test
    void getPhoneNumbersByStation_withDuplicatePhones_returnsDeduplicated() {
        // Arrange
        int stationNumber = 1;
        Person person4 = new Person();
        person4.setFirstName("Alice");
        person4.setLastName("Brown");
        person4.setAddress("456 Oak Ave");
        person4.setPhone("123-456-7890"); // Même téléphone que person1

        List<String> addresses = Arrays.asList("123 Main St", "456 Oak Ave");

        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(addresses);
        when(personRepository.findByAddress("123 Main St")).thenReturn(Arrays.asList(person1));
        when(personRepository.findByAddress("456 Oak Ave")).thenReturn(Arrays.asList(person4));

        // Act
        Set<String> phoneNumbers = firestationService.getPhoneNumbersByStation(stationNumber);

        // Assert
        assertNotNull(phoneNumbers);
        assertEquals(1, phoneNumbers.size());
        assertTrue(phoneNumbers.contains("123-456-7890"));

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
    }

    // ==================== Tests getPersonsCoveredByStation ====================

    @Test
    void getPersonsCoveredByStation_withMultipleAddresses_returnsAllPersons() {
        // Arrange
        int stationNumber = 1;
        List<String> addresses = Arrays.asList("123 Main St", "456 Oak Ave");

        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(addresses);
        when(personRepository.findByAddress("123 Main St")).thenReturn(Arrays.asList(person1, person3));
        when(personRepository.findByAddress("456 Oak Ave")).thenReturn(Arrays.asList(person2));

        // Act
        List<Person> persons = firestationService.getPersonsCoveredByStation(stationNumber);

        // Assert
        assertNotNull(persons);
        assertEquals(3, persons.size());
        assertTrue(persons.contains(person1));
        assertTrue(persons.contains(person2));
        assertTrue(persons.contains(person3));

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
        verify(personRepository, times(1)).findByAddress("123 Main St");
        verify(personRepository, times(1)).findByAddress("456 Oak Ave");
    }

    @Test
    void getPersonsCoveredByStation_withNoAddresses_returnsEmptyList() {
        // Arrange
        int stationNumber = 99;
        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(Collections.emptyList());

        // Act
        List<Person> persons = firestationService.getPersonsCoveredByStation(stationNumber);

        // Assert
        assertNotNull(persons);
        assertTrue(persons.isEmpty());

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
        verify(personRepository, never()).findByAddress(anyString());
    }

    @Test
    void getPersonsCoveredByStation_withOneAddress_returnsPersonsAtAddress() {
        // Arrange
        int stationNumber = 1;
        List<String> addresses = Arrays.asList("123 Main St");

        when(firestationRepository.findAddressesByStation(stationNumber)).thenReturn(addresses);
        when(personRepository.findByAddress("123 Main St")).thenReturn(Arrays.asList(person1, person3));

        // Act
        List<Person> persons = firestationService.getPersonsCoveredByStation(stationNumber);

        // Assert
        assertNotNull(persons);
        assertEquals(2, persons.size());
        assertTrue(persons.contains(person1));
        assertTrue(persons.contains(person3));

        verify(firestationRepository, times(1)).findAddressesByStation(stationNumber);
        verify(personRepository, times(1)).findByAddress("123 Main St");
    }
}
