package com.openclassrooms.safetynetalerts.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodResidentDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationHouseholdDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationsResponseDTO;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

/**
 * Contrôleur REST pour la gestion des alertes inondation.
 * <p>
 * Ce contrôleur expose les endpoints permettant de récupérer les informations
 * des foyers desservis par des stations de pompiers en cas d'inondation :
 * <ul>
 * <li>GET /flood/stations - Récupération des foyers par stations</li>
 * </ul>
 *
 */
@RestController
@RequestMapping("/flood")
public class FloodController {
    private final Logger logger = LoggerFactory.getLogger(FloodController.class);

    private final FirestationService firestationService;
    private final PersonService personService;
    private final MedicalRecordService medicalRecordService;
    private final Utils utils;

    public FloodController(FirestationService firestationService, PersonService personService,
            MedicalRecordService medicalRecordService, Utils utils) {
        this.firestationService = firestationService;
        this.personService = personService;
        this.medicalRecordService = medicalRecordService;
        this.utils = utils;
    }

    /**
     * Récupère tous les foyers desservis par une liste de stations de pompiers.
     * <p>
     * Endpoint : GET /flood/stations?stations={stations}
     * <p>
     * Retourne l'ensemble des foyers (regroupés par adresse) couverts par les
     * stations
     * spécifiées. Pour chaque foyer, les informations suivantes sont fournies :
     * nom,
     * téléphone, âge et dossier médical (médicaments et allergies) de chaque
     * résident.
     * Cette information permet d'organiser efficacement les secours en cas
     * d'inondation.
     *
     * @param stations la liste des numéros de stations de pompiers
     * @return ResponseEntity contenant un {@link FloodStationsResponseDTO} avec la
     *         liste
     *         des foyers regroupés par adresse (HTTP 200)
     */
    @GetMapping("/stations")
    public ResponseEntity<FloodStationsResponseDTO> getPersonsByStations(
            @RequestParam("stations") List<Integer> stations) {

        logger.info("[CALL] GET /flood/stations?stations={}", stations);

        // 1. Récupérer toutes les adresses couvertes par ces stations
        Set<String> addresses = firestationService.getAddressesByStations(stations);

        // 2. Pour chaque adresse, récupérer les personnes et construire les DTOs
        List<FloodStationHouseholdDTO> households = new ArrayList<>();

        for (String address : addresses) {
            // Récupérer les personnes à cette adresse
            List<Person> personsAtAddress = personService.getPersonsByAddress(address);

            // Mapper vers DTOs (avec infos médicales)
            List<FloodResidentDTO> residents = new ArrayList<>();

            for (Person person : personsAtAddress) {
                // Récupérer le dossier médical
                Optional<MedicalRecord> medicalRecordOpt = medicalRecordService.getMedicalRecord(
                        person.getFirstName(),
                        person.getLastName());

                // Extraire medications et allergies
                List<String> medications = medicalRecordOpt
                        .map(MedicalRecord::getMedications)
                        .orElse(Collections.emptyList());

                List<String> allergies = medicalRecordOpt
                        .map(MedicalRecord::getAllergies)
                        .orElse(Collections.emptyList());

                // Créer le DTO
                residents.add(new FloodResidentDTO(
                        person.getLastName(),
                        person.getPhone(),
                        utils.calculateAge(person),
                        new MedicalHistoryDTO(medications, allergies)));
            }

            // Créer le DTO du foyer
            households.add(new FloodStationHouseholdDTO(address, residents));
        }

        // 3. Construire le DTO de réponse
        FloodStationsResponseDTO response = new FloodStationsResponseDTO(households);

        logger.info("[RESPONSE] GET /flood/stations -> {} foyers trouvés", households.size());

        return ResponseEntity.ok(response);
    }
}