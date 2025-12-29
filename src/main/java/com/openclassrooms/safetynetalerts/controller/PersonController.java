package com.openclassrooms.safetynetalerts.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.PersonDTO;
import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.communityemail.CommunityEmailResponseDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResidentDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonMedicalProfileDTO;
import com.openclassrooms.safetynetalerts.mapper.PersonMapper;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrôleur REST pour la gestion des personnes et des informations
 * personnelles.
 * <p>
 * Ce contrôleur expose les endpoints suivants :
 * <ul>
 * <li>GET /personInfo - Récupération des informations médicales par nom</li>
 * <li>GET /fire - Récupération des habitants et numéro de station par
 * adresse</li>
 * <li>GET /communityEmail - Récupération des emails par ville</li>
 * <li>POST /person - Création d'une nouvelle personne</li>
 * <li>PUT /person - Mise à jour d'une personne existante</li>
 * <li>DELETE /person - Suppression d'une personne</li>
 * </ul>
 *
 */
@RestController
@Validated
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;
    private final FirestationService firestationService;
    private final MedicalRecordService medicalRecordService;
    private final Utils utils;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService, FirestationService firestationService,
            MedicalRecordService medicalRecordService, Utils utils, PersonMapper personMapper) {
        this.personService = personService;
        this.firestationService = firestationService;
        this.medicalRecordService = medicalRecordService;
        this.utils = utils;
        this.personMapper = personMapper;
    }

    /**
     * Récupère les informations médicales complètes des personnes par nom de
     * famille.
     * <p>
     * Endpoint : GET /personInfo?lastName={lastName}
     * <p>
     * Retourne la liste de toutes les personnes portant le nom de famille spécifié,
     * avec leurs informations complètes : nom, adresse, âge, email, médicaments et
     * allergies. Cette information est utile pour identifier rapidement les
     * personnes
     * ayant des besoins médicaux spécifiques lors d'interventions d'urgence.
     *
     * @param lastName le nom de famille des personnes recherchées
     * @return ResponseEntity contenant un {@link PersonInfoResponseDTO} avec la
     *         liste
     *         des profils médicaux (HTTP 200)
     */
    @GetMapping("/personInfo")
    public ResponseEntity<PersonInfoResponseDTO> getPersonsByLastName(
            @RequestParam("lastName") String lastName) {

        logger.info("[CALL] GET /personInfo?lastName={}", lastName);

        // 1. Récupérer toutes les personnes avec ce nom
        List<Person> persons = personService.getPersonsByLastName(lastName);

        // 2. Mapper vers DTOs (avec infos médicales)
        List<PersonMedicalProfileDTO> profiles = new ArrayList<>();

        for (Person person : persons) {
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
            profiles.add(new PersonMedicalProfileDTO(
                    person.getLastName(),
                    person.getAddress(),
                    utils.calculateAge(person),
                    person.getEmail(),
                    new MedicalHistoryDTO(medications, allergies)));
        }

        // 3. Construire le DTO de réponse
        PersonInfoResponseDTO response = new PersonInfoResponseDTO(profiles);

        logger.info("[RESPONSE] GET /personInfo -> {} personnes trouvées", profiles.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les habitants d'une adresse avec leurs informations médicales.
     * <p>
     * Endpoint : GET /fire?address={address}
     * <p>
     * Retourne la liste des personnes habitant à l'adresse spécifiée, avec leurs
     * informations médicales (nom, téléphone, âge, médicaments, allergies) ainsi
     * que
     * le numéro de la station de pompiers desservant cette adresse. Cette
     * information
     * est essentielle pour les interventions d'urgence en cas d'incendie.
     *
     * @param address l'adresse pour laquelle on souhaite récupérer les habitants
     * @return ResponseEntity contenant un {@link FireAddressResponseDTO} avec la
     *         liste
     *         des résidents et le numéro de station (HTTP 200)
     */
    @GetMapping("/fire")
    public ResponseEntity<FireAddressResponseDTO> getPersonsByAddress(
            @RequestParam("address") String address) {

        logger.info("[CALL] GET /fire?address={}", address);

        // 1. Récupérer toutes les personnes à l'adresse
        List<Person> personsAtAddress = personService.getPersonsByAddress(address);

        // 2. Récupérer le numéro de station pour cette adresse
        Integer stationNumber = firestationService.getStationNumberByAddress(address);

        // 3. Mapper vers DTOs (avec infos médicales)
        List<FireAddressResidentDTO> residents = new ArrayList<>();

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
            residents.add(new FireAddressResidentDTO(
                    person.getLastName(),
                    person.getPhone(),
                    new MedicalHistoryDTO(medications, allergies),
                    utils.calculateAge(person)));
        }

        // 4. Construire le DTO de réponse
        FireAddressResponseDTO response = new FireAddressResponseDTO(residents, stationNumber);

        logger.info("[RESPONSE] GET /fire -> {} résidents, station={}", residents.size(), stationNumber);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les adresses email de tous les habitants d'une ville.
     * <p>
     * Endpoint : GET /communityEmail?city={city}
     * <p>
     * Retourne l'ensemble des adresses email uniques des personnes habitant dans
     * la ville spécifiée. Cette information permet d'envoyer des communications
     * d'urgence par email à l'ensemble de la communauté.
     *
     * @param city le nom de la ville dont on souhaite récupérer les emails
     * @return ResponseEntity contenant un {@link CommunityEmailResponseDTO} avec
     *         l'ensemble
     *         des emails uniques (HTTP 200)
     */
    @GetMapping("/communityEmail")
    public ResponseEntity<CommunityEmailResponseDTO> getEmailsByCity(
            @RequestParam("city") String city) {

        logger.info("[CALL] GET /communityEmail?city={}", city);

        Set<String> emails = personService.getEmailsByCity(city);

        logger.info("[RESPONSE] GET /communityEmail -> {} emails trouvés", emails.size());

        return ResponseEntity.ok(new CommunityEmailResponseDTO(emails));
    }

    /**
     * Crée une nouvelle personne dans le système.
     * <p>
     * Endpoint : POST /person
     * <p>
     * Permet d'ajouter une nouvelle personne avec ses informations personnelles
     * (prénom, nom, adresse, ville, code postal, téléphone, email). Si une personne
     * avec le même prénom et nom existe déjà, une exception sera levée.
     *
     * @param personDTO le DTO contenant les informations de la personne à créer
     * @return ResponseEntity contenant le {@link PersonDTO} créé (HTTP 201)
     */
    @PostMapping("/person")
    public ResponseEntity<PersonDTO> addPerson(
            @Valid @RequestBody PersonDTO personDTO) {

        logger.info("[CALL] POST /person -> Adding person {} {}",
                personDTO.getFirstName(), personDTO.getLastName());

        // Mapper DTO → Entity
        Person person = personMapper.toEntity(personDTO);

        // Appeler le service
        personService.addPerson(person);

        // Mapper Entity → DTO pour la réponse
        PersonDTO response = personMapper.toDto(person);

        logger.info("[RESPONSE] POST /person -> Person successfully added");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour les informations d'une personne existante.
     * <p>
     * Endpoint : PUT /person?firstName={firstName}&lastName={lastName}
     * <p>
     * Permet de modifier les informations personnelles d'une personne existante
     * (adresse, ville, code postal, téléphone, email). Le prénom et le nom ne
     * peuvent
     * pas être modifiés. Si la personne n'existe pas, une exception sera levée.
     *
     * @param firstName le prénom de la personne à mettre à jour
     * @param lastName  le nom de famille de la personne à mettre à jour
     * @param personDTO le DTO contenant les nouvelles informations de la personne
     * @return ResponseEntity contenant le {@link PersonDTO} mis à jour (HTTP 200)
     */
    @PutMapping("/person")
    public ResponseEntity<PersonDTO> updatePerson(
            @RequestParam("firstName") @NotBlank(message = "First name is required") String firstName,
            @RequestParam("lastName") @NotBlank(message = "Last name is required") String lastName,
            @RequestBody PersonDTO personDTO) {

        logger.info("[CALL] PUT /person -> Updating person {} {}", firstName, lastName);

        // Mapper DTO → Entity
        Person person = personMapper.toEntity(personDTO);
        person.setFirstName(firstName);
        person.setLastName(lastName);

        // Appeler le service
        personService.updatePerson(person);

        // Mapper Entity → DTO pour la réponse
        PersonDTO response = personMapper.toDto(person);

        logger.info("[RESPONSE] PUT /person -> Person successfully updated");
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime une personne du système.
     * <p>
     * Endpoint : DELETE /person?firstName={firstName}&lastName={lastName}
     * <p>
     * Permet de supprimer définitivement une personne et toutes ses informations.
     * Si la personne n'existe pas, une exception sera levée.
     *
     * @param firstName le prénom de la personne à supprimer
     * @param lastName  le nom de famille de la personne à supprimer
     * @return ResponseEntity sans contenu (HTTP 204) en cas de suppression réussie
     */
    @DeleteMapping("/person")
    public ResponseEntity<Void> deletePerson(
            @RequestParam("firstName") @NotBlank(message = "First name is required") String firstName,
            @RequestParam("lastName") @NotBlank(message = "Last name is required") String lastName) {

        logger.info("[CALL] DELETE /person -> Deleting person {} {}", firstName, lastName);

        // Appeler le service
        personService.deletePerson(firstName, lastName);

        logger.info("[RESPONSE] DELETE /person -> Person successfully deleted");
        return ResponseEntity.noContent().build();
    }
}
