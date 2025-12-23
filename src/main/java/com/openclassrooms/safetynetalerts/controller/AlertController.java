package com.openclassrooms.safetynetalerts.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.HouseholdMemberDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationResidentDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

@RestController
public class AlertController {
    private final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private FirestationService firestationService;

    @Autowired
    private PersonService personService;

    @Autowired
    private Utils utils;

    /**
     * GET /firestation?stationNumber=<stationNumber>
     * Retourne les personnes couvertes par une station avec décompte
     * adultes/enfants
     */
    @GetMapping("/firestation")
    public ResponseEntity<FirestationCoverageResponseDTO> getPersonsByStation(
            @RequestParam("stationNumber") int stationNumber) {

        logger.info("[CALL] GET /firestation?stationNumber={}", stationNumber);

        // 1. Appeler le service pour récupérer les personnes (entités brutes)
        List<Person> persons = firestationService.getPersonsCoveredByStation(stationNumber);

        // 2. Calculer les comptages adultes/enfants (logique dans le controller)
        int adultCount = 0;
        int childCount = 0;

        for (Person person : persons) {
            if (utils.isChild(person)) {
                childCount++;
            } else {
                adultCount++;
            }
        }

        // 3. Mapper les entités vers DTOs (mapping dans le controller)
        List<FirestationResidentDTO> residents = persons.stream()
                .map(p -> new FirestationResidentDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAddress(),
                        p.getPhone()))
                .toList();

        // 4. Construire le DTO de réponse
        FirestationCoverageResponseDTO response = new FirestationCoverageResponseDTO(
                residents,
                adultCount,
                childCount);

        logger.info("[RESPONSE] GET /firestation -> {} résidents ({} adultes, {} enfants)",
                residents.size(), adultCount, childCount);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /phoneAlert?firestation=<firestation>
     * Retourne les numéros de téléphone des résidents couverts par une station
     */
    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertResponseDTO> getPhoneByStation(
            @RequestParam("firestation") int firestation) {

        logger.info("[CALL] GET /phoneAlert?firestation={}", firestation);

        // 1. Appeler le service pour récupérer les téléphones
        Set<String> phones = firestationService.getPhonesByStation(firestation);

        // 2. Construire le DTO de réponse (pas de mapping complexe ici)
        PhoneAlertResponseDTO response = new PhoneAlertResponseDTO(phones);

        logger.info("[RESPONSE] GET /phoneAlert -> {} numéros uniques", phones.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /childAlert?address=<address>
     * Retourne la liste des enfants habitant à cette adresse avec les autres
     * membres du foyer
     */
    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertResponseDTO> getChildrenByAddress(
            @RequestParam("address") String address) {

        logger.info("[CALL] GET /childAlert?address={}", address);

        // 1. Appeler le service pour récupérer toutes les personnes à l'adresse
        List<Person> personsAtAddress = personService.getPersonsByAddress(address);

        // 2. Séparer enfants et adultes (logique dans le controller)
        List<Person> children = new ArrayList<>();
        List<Person> adults = new ArrayList<>();

        for (Person person : personsAtAddress) {
            if (utils.isChild(person)) {
                children.add(person);
            } else {
                adults.add(person);
            }
        }

        // 3. Mapper vers DTOs (mapping dans le controller)
        // D'abord les membres du foyer (pour les inclure dans chaque enfant)
        List<HouseholdMemberDTO> householdMembers = adults.stream()
                .map(p -> new HouseholdMemberDTO(p.getFirstName(), p.getLastName()))
                .toList();

        // Ensuite les enfants avec leur âge
        List<ChildInfoDTO> childrenDTOs = children.stream()
                .map(p -> new ChildInfoDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        utils.calculateAge(p),
                        householdMembers))
                .toList();

        // 4. Construire le DTO de réponse
        ChildAlertResponseDTO response = new ChildAlertResponseDTO(childrenDTOs);

        logger.info("[RESPONSE] GET /childAlert -> {} enfants trouvés", childrenDTOs.size());

        return ResponseEntity.ok(response);
    }

}