package com.openclassrooms.safetynetalerts.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.HouseholdMemberDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

/**
 * Contrôleur REST pour la gestion des alertes de sécurité publique.
 * <p>
 * Ce contrôleur expose les endpoints suivants :
 * <ul>
 * <li>GET /firestation - Récupération des personnes couvertes par une
 * station</li>
 * <li>GET /phoneAlert - Récupération des numéros de téléphone par station</li>
 * <li>GET /childAlert - Récupération des enfants à une adresse donnée</li>
 * </ul>
 *
 */
@RestController
public class AlertController {
    private final Logger logger = LoggerFactory.getLogger(AlertController.class);

    private final FirestationService firestationService;
    private final PersonService personService;
    private final Utils utils;

    public AlertController(FirestationService firestationService, PersonService personService, Utils utils) {
        this.firestationService = firestationService;
        this.personService = personService;
        this.utils = utils;
    }

    /**
     * Récupère les numéros de téléphone des résidents couverts par une station de
     * pompiers.
     * <p>
     * Endpoint : GET /phoneAlert?firestation={firestation}
     * <p>
     * Retourne l'ensemble des numéros de téléphone uniques des personnes desservies
     * par
     * la station spécifiée. Cette information permet d'envoyer rapidement des
     * alertes SMS
     * en cas d'urgence (incendie, inondation, etc.).
     *
     * @param firestation le numéro de la station de pompiers dont on veut récupérer
     *                    les numéros de téléphone
     * @return ResponseEntity contenant un {@link PhoneAlertResponseDTO} avec
     *         l'ensemble des numéros
     *         de téléphone uniques (HTTP 200)
     */
    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertResponseDTO> getPhoneByStation(
            @RequestParam("firestation") int firestation) {

        logger.info("[CALL] GET /phoneAlert?firestation={}", firestation);

        // 1. Récupérer les téléphones
        Set<String> phones = firestationService.getPhoneNumbersByStation(firestation);

        // 2. Construire le DTO de réponse
        PhoneAlertResponseDTO response = new PhoneAlertResponseDTO(phones);

        logger.info("[RESPONSE] GET /phoneAlert -> {} numéros uniques", phones.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère la liste des enfants habitant à une adresse donnée.
     * <p>
     * Endpoint : GET /childAlert?address={address}
     * <p>
     * Retourne la liste des enfants (personnes de moins de 18 ans) résidant à
     * l'adresse
     * spécifiée, accompagnée de leurs informations (prénom, nom, âge) et de la
     * liste
     * des autres membres du foyer. Si aucun enfant n'habite à cette adresse, une
     * liste
     * vide est retournée.
     *
     * @param address l'adresse pour laquelle on souhaite récupérer les informations
     *                des enfants
     * @return ResponseEntity contenant un {@link ChildAlertResponseDTO} avec la
     *         liste des enfants
     *         et des membres du foyer (HTTP 200)
     */
    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertResponseDTO> getChildrenByAddress(
            @RequestParam("address") String address) {

        logger.info("[CALL] GET /childAlert?address={}", address);

        // 1. Appeler le service pour récupérer toutes les personnes à l'adresse
        List<Person> personsAtAddress = personService.getPersonsByAddress(address);

        // 2. Séparer enfants et adultes
        List<Person> children = new ArrayList<>();
        List<Person> adults = new ArrayList<>();

        for (Person person : personsAtAddress) {
            if (utils.isChild(person)) {
                children.add(person);
            } else {
                adults.add(person);
            }
        }

        // 3. Mapper vers DTOs
        // Membres du foyer
        List<HouseholdMemberDTO> householdMembers = adults.stream()
                .map(p -> new HouseholdMemberDTO(p.getFirstName(), p.getLastName()))
                .toList();

        // Enfants avec leur âge
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