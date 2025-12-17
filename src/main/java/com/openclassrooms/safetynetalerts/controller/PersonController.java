package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.ChildAlertDTO;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    PersonService personService;

    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertDTO> getChildrenByAdress(
            @RequestParam("address") String address) {
        logger.info("[CALL] childAlert?address={}", address);
        ChildAlertDTO result = personService.getChildrenLivingAtAdress(address);

        return ResponseEntity.ok(result);
    }
}
