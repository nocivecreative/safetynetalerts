package com.openclassrooms.safetynetalerts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    // ==================== Tests addPerson ====================

    @Test
    void addPerson_personAlreadyExists_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.addPerson(person)
        );

        assertEquals("Person already exist", exception.getMessage());
        verify(personRepository, never()).addPerson(any(Person.class));
    }

    // ==================== Tests updatePerson ====================

    @Test
    void updatePerson_personNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.existsByFirstNameAndLastName("John", "Doe")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.updatePerson(person)
        );

        assertEquals("Person not found", exception.getMessage());
        verify(personRepository, never()).updatePerson(any(Person.class));
    }

    // ==================== Tests deletePerson ====================

    @Test
    void deletePerson_personNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(personRepository.existsByFirstNameAndLastName("Unknown", "Person")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> personService.deletePerson("Unknown", "Person")
        );

        assertEquals("Person not found", exception.getMessage());
        verify(personRepository, never()).deletePerson(anyString(), anyString());
    }
}
