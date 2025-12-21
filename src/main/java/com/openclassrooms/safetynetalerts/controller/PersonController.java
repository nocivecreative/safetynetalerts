package com.openclassrooms.safetynetalerts.controller;

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

import com.openclassrooms.safetynetalerts.dto.communityemail.CommunityEmailResponseDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    PersonService personService;

    @GetMapping("/personInfolastName")
    public ResponseEntity<PersonInfoResponseDTO> getPersonsByLastName(
            @RequestParam("lastName") String lastName) { // TODO ne doit pas prendre le param lastName=

        logger.info("[CALL] GET personInfolastName?lastName={}", lastName);
        PersonInfoResponseDTO result = personService.getPersonInfosAndMedicalHistoryByLastName(lastName);

        logger.info("[RESPONSE] GET personInfolastName?lastName={} -> SUCCESS", lastName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fire")
    public ResponseEntity<FireAddressResponseDTO> getPersonsByAdress(
            @RequestParam("address") String address) {

        logger.info("[CALL] GET fire?address={}", address);
        FireAddressResponseDTO result = personService.getPersonAndMedicalHistoryLivingAtAdress(address);

        logger.info("[RESPONSE] GET fire?address={} -> SUCCEESS", address);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/communityEmail")
    public ResponseEntity<CommunityEmailResponseDTO> getEmailsByCity(
            @RequestParam("city") String city) {

        logger.info("[CALL] GET personInfolastName?lastName={}", city);
        CommunityEmailResponseDTO result = personService.getEmailsaddressesForCityResidents(city);

        logger.info("[RESPONSE] GET personInfolastName?lastName={} -> SUCCESS", city);
        return ResponseEntity.ok(result);
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
