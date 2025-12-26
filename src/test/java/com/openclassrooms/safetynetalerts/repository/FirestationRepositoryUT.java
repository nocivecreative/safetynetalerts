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
import com.openclassrooms.safetynetalerts.model.Firestation;

/**
 * Tests unitaires pour FirestationRepository
 *
 * Tests des méthodes CRUD:
 * - findAll, findByStation, findAddressesByStation
 * - existsByStation, existsByAddress
 * - findStationByAddress, findAddressesByStations
 * - addFirestation, updateFirestation
 * - deleteFirestationByAddress, deleteFirestationByStation
 */
@ExtendWith(MockitoExtension.class)
class FirestationRepositoryUT {

    @Mock
    private DataRepo dataRepo;

    @InjectMocks
    private FirestationRepository firestationRepository;

    private DataFile dataFile;
    private List<Firestation> firestations;
    private Firestation firestation1;
    private Firestation firestation2;
    private Firestation firestation3;

    @BeforeEach
    void setUp() {
        // Réinitialiser complètement les données à chaque test pour garantir l'isolation
        firestation1 = createFirestation1();
        firestation2 = createFirestation2();
        firestation3 = createFirestation3();

        firestations = new ArrayList<>(Arrays.asList(firestation1, firestation2, firestation3));

        dataFile = new DataFile();
        dataFile.setFirestations(firestations);

        // Réinitialiser le mock pour éviter toute contamination entre tests
        reset(dataRepo);
        when(dataRepo.loadData()).thenReturn(dataFile);

        // Appeler @PostConstruct manuellement
        firestationRepository.init();
    }

    // Factory methods pour créer des objets neufs à chaque test
    private Firestation createFirestation1() {
        Firestation fs = new Firestation();
        fs.setAddress("123 Main St");
        fs.setStation(1);
        return fs;
    }

    private Firestation createFirestation2() {
        Firestation fs = new Firestation();
        fs.setAddress("456 Oak Ave");
        fs.setStation(2);
        return fs;
    }

    private Firestation createFirestation3() {
        Firestation fs = new Firestation();
        fs.setAddress("789 Pine Rd");
        fs.setStation(1);
        return fs;
    }

    // ==================== Tests findAll ====================

    @Test
    void findAll_returnsAllFirestations() {
        // Act
        List<Firestation> result = firestationRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(firestation1));
        assertTrue(result.contains(firestation2));
        assertTrue(result.contains(firestation3));
    }

    @Test
    void findAll_withEmptyList_returnsEmptyList() {
        // Arrange
        firestations.clear();

        // Act
        List<Firestation> result = firestationRepository.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests findByStation ====================

    @Test
    void findByStation_withExistingStation_returnsMatchingFirestations() {
        // Act
        List<Firestation> result = firestationRepository.findByStation(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(firestation1));
        assertTrue(result.contains(firestation3));
    }

    @Test
    void findByStation_withNonExistingStation_returnsEmptyList() {
        // Act
        List<Firestation> result = firestationRepository.findByStation(99);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests findAddressesByStation ====================

    @Test
    void findAddressesByStation_withExistingStation_returnsAddresses() {
        // Act
        List<String> result = firestationRepository.findAddressesByStation(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("123 Main St"));
        assertTrue(result.contains("789 Pine Rd"));
    }

    @Test
    void findAddressesByStation_withNonExistingStation_returnsEmptyList() {
        // Act
        List<String> result = firestationRepository.findAddressesByStation(99);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests existsByStation ====================

    @Test
    void existsByStation_withExistingStation_returnsTrue() {
        // Act
        boolean result = firestationRepository.existsByStation(1);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByStation_withNonExistingStation_returnsFalse() {
        // Act
        boolean result = firestationRepository.existsByStation(99);

        // Assert
        assertFalse(result);
    }

    // ==================== Tests existsByAddress ====================

    @Test
    void existsByAddress_withExistingAddress_returnsTrue() {
        // Act
        boolean result = firestationRepository.existsByAddress("123 Main St");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByAddress_withNonExistingAddress_returnsFalse() {
        // Act
        boolean result = firestationRepository.existsByAddress("999 Unknown St");

        // Assert
        assertFalse(result);
    }

    // ==================== Tests findStationByAddress ====================

    @Test
    void findStationByAddress_withExistingAddress_returnsStation() {
        // Act
        Optional<Integer> result = firestationRepository.findStationByAddress("123 Main St");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get());
    }

    @Test
    void findStationByAddress_withNonExistingAddress_returnsEmpty() {
        // Act
        Optional<Integer> result = firestationRepository.findStationByAddress("999 Unknown St");

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== Tests findAddressesByStations ====================

    @Test
    void findAddressesByStations_withMultipleStations_returnsAllAddresses() {
        // Act
        Set<String> result = firestationRepository.findAddressesByStations(Arrays.asList(1, 2));

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("123 Main St"));
        assertTrue(result.contains("456 Oak Ave"));
        assertTrue(result.contains("789 Pine Rd"));
    }

    @Test
    void findAddressesByStations_withNonExistingStations_returnsEmptySet() {
        // Act
        Set<String> result = firestationRepository.findAddressesByStations(Arrays.asList(99, 100));

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAddressesByStations_withEmptyList_returnsEmptySet() {
        // Act
        Set<String> result = firestationRepository.findAddressesByStations(Arrays.asList());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Tests addFirestation ====================

    @Test
    void addFirestation_addsNewFirestation() {
        // Arrange
        Firestation newFirestation = new Firestation();
        newFirestation.setAddress("999 New St");
        newFirestation.setStation(3);

        int initialSize = firestations.size();

        // Act
        firestationRepository.addFirestation(newFirestation);

        // Assert
        assertEquals(initialSize + 1, firestations.size());
        assertTrue(firestations.contains(newFirestation));
    }

    // ==================== Tests updateFirestation ====================

    @Test
    void updateFirestation_updatesExistingFirestation() {
        // Arrange
        Firestation updatedFirestation = new Firestation();
        updatedFirestation.setAddress("123 Updated St");
        updatedFirestation.setStation(1);

        int initialSize = firestations.size();

        // Act
        firestationRepository.updateFirestation(updatedFirestation);

        // Assert
        // La méthode updateFirestation supprime TOUS les firestations avec station=1 (2 éléments)
        // puis ajoute le nouveau (1 élément). Donc: 3 - 2 + 1 = 2
        assertEquals(initialSize - 1, firestations.size());
        assertTrue(firestations.contains(updatedFirestation));
        // Les anciennes adresses avec station=1 devraient être supprimées
        assertFalse(firestations.stream().anyMatch(f -> f.getAddress().equals("123 Main St")));
        assertFalse(firestations.stream().anyMatch(f -> f.getAddress().equals("789 Pine Rd")));
    }

    // ==================== Tests deleteFirestationByAddress ====================

    @Test
    void deleteFirestationByAddress_removesFirestation() {
        // Arrange
        int initialSize = firestations.size();

        // Act
        firestationRepository.deleteFirestationByAddress("123 Main St");

        // Assert
        assertEquals(initialSize - 1, firestations.size());
        assertFalse(firestations.contains(firestation1));
        assertTrue(firestations.contains(firestation2));
        assertTrue(firestations.contains(firestation3));
    }

    @Test
    void deleteFirestationByAddress_withNonExistingAddress_doesNothing() {
        // Arrange
        int initialSize = firestations.size();

        // Act
        firestationRepository.deleteFirestationByAddress("999 Unknown St");

        // Assert
        assertEquals(initialSize, firestations.size());
    }

    // ==================== Tests deleteFirestationByStation ====================

    @Test
    void deleteFirestationByStation_removesAllFirestationsWithStation() {
        // Arrange
        int initialSize = firestations.size();

        // Act
        firestationRepository.deleteFirestationByStation(1);

        // Assert
        assertEquals(initialSize - 2, firestations.size());
        assertFalse(firestations.contains(firestation1));
        assertFalse(firestations.contains(firestation3));
        assertTrue(firestations.contains(firestation2));
    }

    @Test
    void deleteFirestationByStation_withNonExistingStation_doesNothing() {
        // Arrange
        int initialSize = firestations.size();

        // Act
        firestationRepository.deleteFirestationByStation(99);

        // Assert
        assertEquals(initialSize, firestations.size());
    }
}
