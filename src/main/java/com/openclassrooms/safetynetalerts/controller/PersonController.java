package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.communityemail.CommunityEmailResponseDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    PersonService personService;

    @GetMapping("/personInfolastName")
    public ResponseEntity<PersonInfoResponseDTO> getPersonsByLastName(
            @RequestParam("lastName") String lastName) {

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

}
