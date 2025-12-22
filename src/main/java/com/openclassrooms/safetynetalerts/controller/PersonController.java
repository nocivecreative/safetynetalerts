package com.openclassrooms.safetynetalerts.controller;

import java.util.List;
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

import com.openclassrooms.safetynetalerts.PersonProfile;
import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.communityemail.CommunityEmailResponseDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResidentDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonMedicalProfileDTO;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.service.PersonProfileService;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class PersonController {
        private final Logger logger = LoggerFactory.getLogger(PersonController.class);

        @Autowired
        PersonService personService;

        @Autowired
        FirestationRepository firestationRepository;

        @Autowired
        private PersonProfileService personProfileService;

        @GetMapping("/personInfolastName")
        public ResponseEntity<PersonInfoResponseDTO> getPersonsByLastName(
                        @RequestParam("lastName") String lastName) {

                logger.info("[CALL] GET personInfolastName?lastName={}", lastName);

                List<PersonProfile> profiles = personProfileService.getProfilesByLastName(lastName);

                List<PersonMedicalProfileDTO> result = profiles.stream()
                                .map(profile -> new PersonMedicalProfileDTO(
                                                profile.getPerson().getLastName(),
                                                profile.getPerson().getAddress(),
                                                profile.getAge(),
                                                profile.getPerson().getEmail(),
                                                new MedicalHistoryDTO(
                                                                profile.getMedications(),
                                                                profile.getAllergies())))
                                .toList();

                logger.info("[RESPONSE] GET personInfolastName?lastName={} -> SUCCESS", lastName);
                return ResponseEntity.ok(new PersonInfoResponseDTO(result));
        }

        @GetMapping("/fire")
        public ResponseEntity<FireAddressResponseDTO> getPersonsByAdress(
                        @RequestParam("address") String address) {

                logger.info("[CALL] GET fire?address={}", address);

                List<PersonProfile> profiles = personProfileService.getProfilesByAddress(address);

                int stationNumber = firestationRepository
                                .findStationByAddress(address)
                                .orElse(-1);

                List<FireAddressResidentDTO> residents = profiles.stream()
                                .map(profile -> new FireAddressResidentDTO(
                                                profile.getPerson().getLastName(),
                                                profile.getPerson().getPhone(),
                                                new MedicalHistoryDTO(
                                                                profile.getMedications(),
                                                                profile.getAllergies()),
                                                profile.getAge()))
                                .toList();

                logger.info("[RESPONSE] GET fire?address={} -> SUCCESS", address);
                return ResponseEntity.ok(new FireAddressResponseDTO(residents, stationNumber));
        }

        @GetMapping("/communityEmail")
        public ResponseEntity<CommunityEmailResponseDTO> getEmailsByCity(
                        @RequestParam("city") String city) {

                Set<String> emails = personService.getEmailsByCity(city);
                return ResponseEntity.ok(new CommunityEmailResponseDTO(emails));
        }

        @PostMapping("/person")
        public ResponseEntity<Void> addPerson(@RequestBody Person person) {
                logger.debug("[CALL] POST person -> Adding person {} {}",
                                person.getFirstName(), person.getLastName());
                personService.addPerson(person);
                logger.debug("[RESPONSE] POST person -> Person successfully added : {} {}",
                                person.getFirstName(), person.getLastName());
                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @PutMapping("/person")
        public ResponseEntity<Void> updatePerson(@RequestBody Person person) {
                logger.debug("[CALL] PUT person -> Update person {} {}",
                                person.getFirstName(), person.getLastName());
                personService.updatePerson(person);
                logger.debug("[RESPONSE] PUT person -> person successfully updated {} {}",
                                person.getFirstName(), person.getLastName());
                return ResponseEntity.ok().build();
        }

        @DeleteMapping("/person")
        public ResponseEntity<Void> deletePerson(
                        @RequestParam String firstName,
                        @RequestParam String lastName) {
                logger.debug("[CALL] DELETE person -> deleting person {} {}",
                                firstName, lastName);
                personService.deletePerson(firstName, lastName);
                logger.debug("[RESPONSE] DELETE person -> person successfully deleted {} {}",
                                firstName, lastName);
                return ResponseEntity.noContent().build();
        }

}
