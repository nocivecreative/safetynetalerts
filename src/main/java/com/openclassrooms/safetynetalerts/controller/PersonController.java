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
     * GET /personInfo?lastName=<lastName> (Erreur CDC)
     * Retourne les infos et dossier médical des personnes avec ce nom
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
     * GET /fire?address=<address>
     * Retourne les habitants à une adresse avec leurs infos médicales et le numéro
     * de station
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

    @GetMapping("/communityEmail")
    public ResponseEntity<CommunityEmailResponseDTO> getEmailsByCity(
            @RequestParam("city") String city) {

        Set<String> emails = personService.getEmailsByCity(city);
        return ResponseEntity.ok(new CommunityEmailResponseDTO(emails));
    }

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
