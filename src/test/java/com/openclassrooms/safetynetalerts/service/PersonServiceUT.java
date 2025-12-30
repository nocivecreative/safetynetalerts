package com.openclassrooms.safetynetalerts.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

/**
 * Tests unitaires pour PersonService
 *
 * Tests critiques uniquement:
 * - Gestion des exceptions (IllegalArgumentException) pour CRUD
 */
@ExtendWith(MockitoExtension.class)
class PersonServiceUT {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setAddress("123 Main St");
        person.setCity("Paris");
        person.setZip("75001");
        person.setPhone("123-456-7890");
        person.setEmail("john.doe@email.com");
    }

    // ==================== Test getPersonsByAddress ===========

    @Test
    void getPersonsByAddress_ExistingPerson_ReturnOK() {
        // Arrange
        Person person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        person2.setAddress("123 Main St");

        List<Person> expectedPersons = Arrays.asList(person, person2);
        when(personRepository.findByAddress("123 Main St")).thenReturn(expectedPersons);

        // Act
        List<Person> result = personService.getPersonsByAddress("123 Main St");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(person));
        assertTrue(result.contains(person2));
        verify(personRepository).findByAddress("123 Main St");
    }

    // ==================== Test getPersonsByLastName ===========

    @Test
    void getPersonsByLastName_ExistingPerson_ReturnOK() {
        // Arrange
        Person person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Doe");
        person2.setAddress("123 Main St");

        List<Person> expectedPersons = Arrays.asList(person, person2);
        when(personRepository.findByLastName("Doe")).thenReturn(expectedPersons);

        // Act
        List<Person> result = personService.getPersonsByLastName("Doe");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(person));
        assertTrue(result.contains(person2));
        verify(personRepository).findByLastName("Doe");
    }

    // ==================== Tests addPerson ====================

    @Test
    void addPerson_personAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.addPerson(person));

        assertEquals("Person already exist", exception.getMessage());
        verify(personRepository, never()).addPerson(any(Person.class));
    }

    // ==================== Tests updatePerson ====================

    @Test
    void updatePerson_personNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.updatePerson("John", "Doe", person));

        assertEquals("Person not found: John Doe", exception.getMessage());
        verify(personRepository, never()).updatePerson(any(Person.class), any(Person.class));
    }

    // ==================== Tests deletePerson ====================

    @Test
    void deletePerson_personNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.existsByFirstNameAndLastName("Unknown", "Person")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.deletePerson("Unknown", "Person"));

        assertEquals("Person not found: Unknown Person", exception.getMessage());
        verify(personRepository, never()).deletePerson(anyString(), anyString());
    }
}
