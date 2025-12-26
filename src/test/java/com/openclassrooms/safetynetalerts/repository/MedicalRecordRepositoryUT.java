package com.openclassrooms.safetynetalerts.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;

/**
 * Tests unitaires pour MedicalRecordRepository
 *
 * Tests des méthodes CRUD:
 * - findAll, findByFirstNameAndLastName
 * - existsByFirstNameAndLastName
 * - save (create/update), update, delete
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordRepositoryUT {

    @Mock
    private DataRepo dataRepo;

    @InjectMocks
    private MedicalRecordRepository medicalRecordRepository;

    private DataFile dataFile;
    private List<MedicalRecord> medicalRecords;
    private MedicalRecord record1;
    private MedicalRecord record2;
    private MedicalRecord record3;

    @BeforeEach
    void setUp() {
        // Réinitialiser complètement les données à chaque test pour garantir l'isolation
        record1 = createRecord1();
        record2 = createRecord2();
        record3 = createRecord3();

        medicalRecords = new ArrayList<>(Arrays.asList(record1, record2, record3));

        dataFile = new DataFile();
        dataFile.setMedicalrecords(medicalRecords);

        // Réinitialiser le mock pour éviter toute contamination entre tests
        reset(dataRepo);
        when(dataRepo.loadData()).thenReturn(dataFile);

        // Appeler @PostConstruct manuellement
        medicalRecordRepository.init();
    }

    // Factory methods pour créer des objets neufs à chaque test
    private MedicalRecord createRecord1() {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName("John");
        mr.setLastName("Doe");
        mr.setBirthdate("01/01/1980");
        mr.setMedications(Arrays.asList("medication1:100mg", "medication2:50mg"));
        mr.setAllergies(Arrays.asList("peanut", "shellfish"));
        return mr;
    }

    private MedicalRecord createRecord2() {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName("Jane");
        mr.setLastName("Smith");
        mr.setBirthdate("05/15/1985");
        mr.setMedications(Arrays.asList("aspirin:500mg"));
        mr.setAllergies(new ArrayList<>());
        return mr;
    }

    private MedicalRecord createRecord3() {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName("Bob");
        mr.setLastName("Johnson");
        mr.setBirthdate("10/20/2015");
        mr.setMedications(new ArrayList<>());
        mr.setAllergies(Arrays.asList("lactose"));
        return mr;
    }

    // ==================== Tests findAll ====================

    @Test
    void findAll_returnsAllMedicalRecords() {
        // Act
        List<MedicalRecord> result = medicalRecordRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(record1));
        assertTrue(result.contains(record2));
        assertTrue(result.contains(record3));
    }

    @Test
    void findAll_withEmptyList_returnsEmptyList() {
        // Arrange
        medicalRecords.clear();

        // Act
        List<MedicalRecord> result = medicalRecordRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests findByFirstNameAndLastName ====================

    @Test
    void findByFirstNameAndLastName_withExistingRecord_returnsRecord() {
        // Act
        Optional<MedicalRecord> result = medicalRecordRepository.findByFirstNameAndLastName("John", "Doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(record1, result.get());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        assertEquals("01/01/1980", result.get().getBirthdate());
    }

    @Test
    void findByFirstNameAndLastName_withNonExistingRecord_returnsEmpty() {
        // Act
        Optional<MedicalRecord> result = medicalRecordRepository.findByFirstNameAndLastName("Unknown", "Person");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByFirstNameAndLastName_withPartialMatch_returnsEmpty() {
        // Act - Bon prénom, mauvais nom
        Optional<MedicalRecord> result = medicalRecordRepository.findByFirstNameAndLastName("John", "Smith");

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== Tests existsByFirstNameAndLastName ====================

    @Test
    void existsByFirstNameAndLastName_withExistingRecord_returnsTrue() {
        // Act
        boolean result = medicalRecordRepository.existsByFirstNameAndLastName("John", "Doe");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByFirstNameAndLastName_withNonExistingRecord_returnsFalse() {
        // Act
        boolean result = medicalRecordRepository.existsByFirstNameAndLastName("Unknown", "Person");

        // Assert
        assertFalse(result);
    }

    // ==================== Tests save ====================

    @Test
    void save_withNewRecord_addsRecord() {
        // Arrange
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setFirstName("Alice");
        newRecord.setLastName("Brown");
        newRecord.setBirthdate("03/15/1990");
        newRecord.setMedications(Arrays.asList("med1:10mg"));
        newRecord.setAllergies(Arrays.asList("pollen"));

        int initialSize = medicalRecords.size();

        // Act
        MedicalRecord result = medicalRecordRepository.save(newRecord);

        // Assert
        assertNotNull(result);
        assertEquals(newRecord, result);
        assertEquals(initialSize + 1, medicalRecords.size());
        assertTrue(medicalRecords.contains(newRecord));
    }

    @Test
    void save_withExistingRecord_updatesRecord() {
        // Arrange
        MedicalRecord updatedRecord = new MedicalRecord();
        updatedRecord.setFirstName("John");
        updatedRecord.setLastName("Doe");
        updatedRecord.setBirthdate("02/02/1981");
        updatedRecord.setMedications(Arrays.asList("new_med:200mg"));
        updatedRecord.setAllergies(Arrays.asList("gluten"));

        int initialSize = medicalRecords.size();

        // Act
        MedicalRecord result = medicalRecordRepository.save(updatedRecord);

        // Assert
        assertNotNull(result);
        assertEquals(initialSize, medicalRecords.size()); // Pas d'ajout, juste une mise à jour

        // Vérifier que l'enregistrement existant a été mis à jour
        Optional<MedicalRecord> found = medicalRecordRepository.findByFirstNameAndLastName("John", "Doe");
        assertTrue(found.isPresent());
        assertEquals("02/02/1981", found.get().getBirthdate());
        assertEquals(Arrays.asList("new_med:200mg"), found.get().getMedications());
        assertEquals(Arrays.asList("gluten"), found.get().getAllergies());
    }

    @Test
    void save_withPartialUpdate_updatesOnlyProvidedFields() {
        // Arrange
        MedicalRecord partialUpdate = new MedicalRecord();
        partialUpdate.setFirstName("John");
        partialUpdate.setLastName("Doe");
        partialUpdate.setBirthdate("03/03/1982"); // Nouvelle date
        // medications et allergies = null (ne doivent pas être modifiés)

        // Act
        medicalRecordRepository.save(partialUpdate);

        // Assert
        Optional<MedicalRecord> found = medicalRecordRepository.findByFirstNameAndLastName("John", "Doe");
        assertTrue(found.isPresent());
        assertEquals("03/03/1982", found.get().getBirthdate());
        // Les medications et allergies originaux doivent être préservés
        assertEquals(Arrays.asList("medication1:100mg", "medication2:50mg"), found.get().getMedications());
        assertEquals(Arrays.asList("peanut", "shellfish"), found.get().getAllergies());
    }

    // ==================== Tests update ====================

    @Test
    void update_withExistingRecord_updatesAndReturnsRecord() {
        // Arrange
        MedicalRecord updatedRecord = new MedicalRecord();
        updatedRecord.setBirthdate("04/04/1983");
        updatedRecord.setMedications(Arrays.asList("updated_med:300mg"));
        updatedRecord.setAllergies(Arrays.asList("dust"));

        // Act
        Optional<MedicalRecord> result = medicalRecordRepository.update("John", "Doe", updatedRecord);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        assertEquals("04/04/1983", result.get().getBirthdate());
        assertEquals(Arrays.asList("updated_med:300mg"), result.get().getMedications());
        assertEquals(Arrays.asList("dust"), result.get().getAllergies());
    }

    @Test
    void update_withNonExistingRecord_returnsEmpty() {
        // Arrange
        MedicalRecord updatedRecord = new MedicalRecord();
        updatedRecord.setBirthdate("01/01/2000");

        // Act
        Optional<MedicalRecord> result = medicalRecordRepository.update("Unknown", "Person", updatedRecord);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void update_withPartialData_updatesOnlyProvidedFields() {
        // Arrange
        MedicalRecord partialUpdate = new MedicalRecord();
        partialUpdate.setMedications(Arrays.asList("only_meds:100mg"));
        // birthdate et allergies = null (ne doivent pas être modifiés)

        // Act
        medicalRecordRepository.update("John", "Doe", partialUpdate);

        // Assert
        Optional<MedicalRecord> found = medicalRecordRepository.findByFirstNameAndLastName("John", "Doe");
        assertTrue(found.isPresent());
        assertEquals("01/01/1980", found.get().getBirthdate()); // Préservé
        assertEquals(Arrays.asList("only_meds:100mg"), found.get().getMedications()); // Mis à jour
        assertEquals(Arrays.asList("peanut", "shellfish"), found.get().getAllergies()); // Préservé
    }

    // ==================== Tests delete ====================

    @Test
    void delete_withExistingRecord_removesRecordAndReturnsTrue() {
        // Arrange
        int initialSize = medicalRecords.size();

        // Act
        boolean result = medicalRecordRepository.delete("John", "Doe");

        // Assert
        assertTrue(result);
        assertEquals(initialSize - 1, medicalRecords.size());
        assertFalse(medicalRecords.contains(record1));
        assertTrue(medicalRecords.contains(record2));
        assertTrue(medicalRecords.contains(record3));
    }

    @Test
    void delete_withNonExistingRecord_returnsFalse() {
        // Arrange
        int initialSize = medicalRecords.size();

        // Act
        boolean result = medicalRecordRepository.delete("Unknown", "Person");

        // Assert
        assertFalse(result);
        assertEquals(initialSize, medicalRecords.size());
    }

    @Test
    void delete_withPartialMatch_returnsFalse() {
        // Arrange
        int initialSize = medicalRecords.size();

        // Act - Bon prénom, mauvais nom
        boolean result = medicalRecordRepository.delete("John", "Smith");

        // Assert
        assertFalse(result);
        assertEquals(initialSize, medicalRecords.size());
        assertTrue(medicalRecords.contains(record1));
    }
}
