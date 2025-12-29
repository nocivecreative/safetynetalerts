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
        // Réinitialiser complètement les données à chaque test pour garantir
        // l'isolation
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

    // ==================== Tests existsByStation ====================

    @Test
    void existsByStation_withExistingStation_returnsTrue() {
        // Act
        boolean result = firestationRepository.existsByStation(1);

        // Assert
        assertTrue(result);
    }

    // ==================== Tests existsByAddress ====================

    @Test
    void existsByAddress_withExistingAddress_returnsTrue() {
        // Act
        boolean result = firestationRepository.existsByAddress("123 Main St");

        // Assert
        assertTrue(result);
    }

    // ==================== Tests findStationByAddress ====================

    @Test
    void findStationByAddress_withExistingAddress_returnsStation() {
        // Act
        Optional<Integer> result = firestationRepository.findStationNumberByAddress("123 Main St");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get());
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
        updatedFirestation.setAddress("123 Main St");
        updatedFirestation.setStation(99);

        // int initialSize = firestations.size();

        // Act
        firestationRepository.updateFirestation(updatedFirestation);

        // Assert
        assertTrue(firestations.contains(updatedFirestation));
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

}
