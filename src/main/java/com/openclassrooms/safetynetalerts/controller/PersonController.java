package com.openclassrooms.safetynetalerts.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

@RestController
public class PersonController {
        private final Logger logger = LoggerFactory.getLogger(PersonController.class);

        @Autowired
        private PersonService personService;

        @Autowired
        private FirestationRepository firestationRepository;

        @Autowired
        private MedicalRecordService medicalRecordService;

        @Autowired
        private Utils utils;

        /**
         * GET /personInfolastName?lastName=<lastName>
         * Retourne les infos et dossier médical des personnes avec ce nom
         */
        @GetMapping("/personInfolastName")
        public ResponseEntity<PersonInfoResponseDTO> getPersonsByLastName(
                        @RequestParam("lastName") String lastName) {

                logger.info("[CALL] GET /personInfolastName?lastName={}", lastName);

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

                logger.info("[RESPONSE] GET /personInfolastName -> {} personnes trouvées", profiles.size());

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
                Integer stationNumber = firestationRepository
                                .findStationByAddress(address)
                                .orElse(null);

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
        public ResponseEntity<PersonDTO> addPerson(@RequestBody PersonDTO personDTO) {
                logger.info("[CALL] POST /person -> Adding person {} {}",
                                personDTO.getFirstName(), personDTO.getLastName());

                // Mapper DTO → Entity
                Person person = mapDtoToEntity(personDTO);

                // Appeler le service
                personService.addPerson(person);

                // Mapper Entity → DTO pour la réponse
                PersonDTO response = mapEntityToDto(person);

                logger.info("[RESPONSE] POST /person -> Person successfully added");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/person")
        public ResponseEntity<PersonDTO> updatePerson(@RequestBody PersonDTO personDTO) {
                logger.info("[CALL] PUT /person -> Updating person {} {}",
                                personDTO.getFirstName(), personDTO.getLastName());

                // Mapper DTO → Entity
                Person person = mapDtoToEntity(personDTO);

                // Appeler le service
                personService.updatePerson(person);

                // Mapper Entity → DTO pour la réponse
                PersonDTO response = mapEntityToDto(person);

                logger.info("[RESPONSE] PUT /person -> Person successfully updated");
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/person")
        public ResponseEntity<Void> deletePerson(
                        @RequestParam String firstName,
                        @RequestParam String lastName) {

                logger.info("[CALL] DELETE /person -> Deleting person {} {}", firstName, lastName);

                // Appeler le service
                personService.deletePerson(firstName, lastName);

                logger.info("[RESPONSE] DELETE /person -> Person successfully deleted");
                return ResponseEntity.noContent().build();
        }

        // --- Méthodes privées de mapping ---

        private Person mapDtoToEntity(PersonDTO dto) {
                Person person = new Person();
                person.setFirstName(dto.getFirstName());
                person.setLastName(dto.getLastName());
                person.setAddress(dto.getAddress());
                person.setCity(dto.getCity());
                person.setZip(dto.getZip());
                person.setPhone(dto.getPhone());
                person.setEmail(dto.getEmail());
                return person;
        }

        private PersonDTO mapEntityToDto(Person person) {
                return new PersonDTO(
                                person.getFirstName(),
                                person.getLastName(),
                                person.getAddress(),
                                person.getCity(),
                                person.getZip(),
                                person.getPhone(),
                                person.getEmail());
        }
}
