package com.openclassrooms.safetynetalerts.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        // Réinitialiser complètement les données à chaque test pour garantir l'isolation
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

    @Test
    void findAll_withEmptyList_returnsEmptyList() {
        // Arrange
        persons.clear();

        // Act
        List<Person> result = personRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    @Test
    void findByAddress_withNonExistingAddress_returnsEmptyList() {
        // Act
        List<Person> result = personRepository.findByAddress("999 Unknown St");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    @Test
    void findByFirstNameAndLastName_withNonExistingPerson_returnsEmpty() {
        // Act
        Optional<Person> result = personRepository.findByFirstNameAndLastName("Unknown", "Person");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByFirstNameAndLastName_withPartialMatch_returnsEmpty() {
        // Act - Chercher avec bon prénom mais mauvais nom
        Optional<Person> result = personRepository.findByFirstNameAndLastName("John", "Smith");

        // Assert
        assertFalse(result.isPresent());
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

    @Test
    void findByLastName_withNonExistingLastName_returnsEmptyList() {
        // Act
        List<Person> result = personRepository.findByLastName("Unknown");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
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

    @Test
    void findEmailsByCity_withNonExistingCity_returnsEmptySet() {
        // Act
        Set<String> result = personRepository.findEmailsByCity("Unknown City");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findEmailsByCity_withDuplicateEmails_returnsDeduplicated() {
        // Arrange
        Person person4 = new Person();
        person4.setFirstName("Alice");
        person4.setLastName("Doe");
        person4.setCity("Springfield");
        person4.setEmail("john.doe@email.com"); // Même email que person1
        persons.add(person4);

        // Act
        Set<String> result = personRepository.findEmailsByCity("Springfield");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Seulement 2 emails uniques
        assertTrue(result.contains("john.doe@email.com"));
        assertTrue(result.contains("bob.doe@email.com"));
    }

    // ==================== Tests existsByFirstNameAndLastName ====================

    @Test
    void existsByFirstNameAndLastName_withExistingPerson_returnsTrue() {
        // Act
        boolean result = personRepository.existsByFirstNameAndLastName("John", "Doe");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByFirstNameAndLastName_withNonExistingPerson_returnsFalse() {
        // Act
        boolean result = personRepository.existsByFirstNameAndLastName("Unknown", "Person");

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByFirstNameAndLastName_withPartialMatch_returnsFalse() {
        // Act - Bon prénom, mauvais nom
        boolean result = personRepository.existsByFirstNameAndLastName("John", "Smith");

        // Assert
        assertFalse(result);
    }

    // ==================== Tests addPerson ====================

    @Test
    void addPerson_addsNewPerson() {
        // Arrange
        Person newPerson = new Person();
        newPerson.setFirstName("Alice");
        newPerson.setLastName("Brown");
        newPerson.setAddress("999 New St");
        newPerson.setCity("Springfield");
        newPerson.setEmail("alice.brown@email.com");

        int initialSize = persons.size();

        // Act
        personRepository.addPerson(newPerson);

        // Assert
        assertEquals(initialSize + 1, persons.size());
        assertTrue(persons.contains(newPerson));
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
        personRepository.updatePerson(updatedPerson);

        // Assert
        assertEquals(initialSize, persons.size());
        assertTrue(persons.contains(updatedPerson));

        // Vérifier que l'ancienne personne a été supprimée
        Optional<Person> result = personRepository.findByFirstNameAndLastName("John", "Doe");
        assertTrue(result.isPresent());
        assertEquals("999 Updated St", result.get().getAddress());
        assertEquals("New City", result.get().getCity());
    }

    @Test
    void updatePerson_withNonExistingPerson_addsNewPerson() {
        // Arrange
        Person newPerson = new Person();
        newPerson.setFirstName("Unknown");
        newPerson.setLastName("Person");
        newPerson.setAddress("999 New St");

        int initialSize = persons.size();

        // Act
        personRepository.updatePerson(newPerson);

        // Assert
        assertEquals(initialSize + 1, persons.size());
        assertTrue(persons.contains(newPerson));
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

    @Test
    void deletePerson_withNonExistingPerson_doesNothing() {
        // Arrange
        int initialSize = persons.size();

        // Act
        personRepository.deletePerson("Unknown", "Person");

        // Assert
        assertEquals(initialSize, persons.size());
    }

    @Test
    void deletePerson_withPartialMatch_doesNothing() {
        // Arrange
        int initialSize = persons.size();

        // Act - Bon prénom, mauvais nom
        personRepository.deletePerson("John", "Smith");

        // Assert
        assertEquals(initialSize, persons.size());
        assertTrue(persons.contains(person1));
    }
}
