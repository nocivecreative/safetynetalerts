package com.openclassrooms.safetynetalerts.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Person;

/**
 * Tests unitaires pour PersonRepository
 *
 * Tests des méthodes CRUD:
 * - findAll, findByAddress, findByFirstNameAndLastName
 * - findByLastName, findEmailsByCity
 * - existsByFirstNameAndLastName
 * - addPerson, updatePerson, deletePerson
 */
@ExtendWith(MockitoExtension.class)
class PersonRepositoryUT {

    @Mock
    private DataRepo dataRepo;

    @InjectMocks
    private PersonRepository personRepository;

    private DataFile dataFile;
    private List<Person> persons;
    private Person person1;
    private Person person2;
    private Person person3;

    @BeforeEach
    void setUp() {
        // Réinitialiser complètement les données à chaque test pour garantir
        // l'isolation
        person1 = createPerson1();
        person2 = createPerson2();
        person3 = createPerson3();

        persons = new ArrayList<>(Arrays.asList(person1, person2, person3));

        dataFile = new DataFile();
        dataFile.setPersons(persons);

        // Réinitialiser le mock pour éviter toute contamination entre tests
        reset(dataRepo);
        when(dataRepo.loadData()).thenReturn(dataFile);

        // Appeler @PostConstruct manuellement
        personRepository.init();
    }

    // Factory methods pour créer des objets neufs à chaque test
    private Person createPerson1() {
        Person p = new Person();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setAddress("123 Main St");
        p.setCity("Springfield");
        p.setZip("12345");
        p.setPhone("123-456-7890");
        p.setEmail("john.doe@email.com");
        return p;
    }

    private Person createPerson2() {
        Person p = new Person();
        p.setFirstName("Jane");
        p.setLastName("Smith");
        p.setAddress("456 Oak Ave");
        p.setCity("Shelbyville");
        p.setZip("67890");
        p.setPhone("456-789-0123");
        p.setEmail("jane.smith@email.com");
        return p;
    }

    private Person createPerson3() {
        Person p = new Person();
        p.setFirstName("Bob");
        p.setLastName("Doe");
        p.setAddress("123 Main St");
        p.setCity("Springfield");
        p.setZip("12345");
        p.setPhone("789-012-3456");
        p.setEmail("bob.doe@email.com");
        return p;
    }

    // ==================== Tests findAll ====================

    @Test
    void findAll_returnsAllPersons() {
        // Act
        List<Person> result = personRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person2));
        assertTrue(result.contains(person3));
    }

    // ==================== Tests findByAddress ====================

    @Test
    void findByAddress_withExistingAddress_returnsMatchingPersons() {
        // Act
        List<Person> result = personRepository.findByAddress("123 Main St");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person3));
    }

    // ==================== Tests findByFirstNameAndLastName ====================

    @Test
    void findByFirstNameAndLastName_withExistingPerson_returnsPerson() {
        // Act
        Optional<Person> result = personRepository.findByFirstNameAndLastName("John", "Doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(person1, result.get());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    // ==================== Tests findByLastName ====================

    @Test
    void findByLastName_withExistingLastName_returnsMatchingPersons() {
        // Act
        List<Person> result = personRepository.findByLastName("Doe");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(person1));
        assertTrue(result.contains(person3));
    }

    // ==================== Tests findEmailsByCity ====================

    @Test
    void findEmailsByCity_withExistingCity_returnsEmails() {
        // Act
        Set<String> result = personRepository.findEmailsByCity("Springfield");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("john.doe@email.com"));
        assertTrue(result.contains("bob.doe@email.com"));
    }

    // ==================== Tests updatePerson ====================

    @Test
    void updatePerson_updatesExistingPerson() {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("John");
        updatedPerson.setLastName("Doe");
        updatedPerson.setAddress("999 Updated St");
        updatedPerson.setCity("New City");
        updatedPerson.setZip("99999");
        updatedPerson.setPhone("999-999-9999");
        updatedPerson.setEmail("john.updated@email.com");

        int initialSize = persons.size();

        // Act
        personRepository.updatePerson(person1, updatedPerson);

        // Assert
        assertEquals(initialSize, persons.size());

        // Vérifier que person1 a été mise à jour
        assertEquals("999 Updated St", person1.getAddress());
        assertEquals("New City", person1.getCity());
        assertEquals("99999", person1.getZip());
        assertEquals("999-999-9999", person1.getPhone());
        assertEquals("john.updated@email.com", person1.getEmail());
    }

    // ==================== Tests deletePerson ====================

    @Test
    void deletePerson_removesExistingPerson() {
        // Arrange
        int initialSize = persons.size();

        // Act
        personRepository.deletePerson("John", "Doe");

        // Assert
        assertEquals(initialSize - 1, persons.size());
        assertFalse(persons.contains(person1));
        assertTrue(persons.contains(person2));
        assertTrue(persons.contains(person3));
    }
}
