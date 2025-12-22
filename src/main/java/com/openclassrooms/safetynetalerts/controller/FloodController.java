package com.openclassrooms.safetynetalerts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.HouseholdProfile;
import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodResidentDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationHouseholdDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationsResponseDTO;
import com.openclassrooms.safetynetalerts.service.FloodProfileService;

@RestController
@RequestMapping("/flood")
public class FloodController {
    private final Logger logger = LoggerFactory.getLogger(FloodController.class);

    @Autowired
    private FloodProfileService floodProfileService;

    @GetMapping("/stations")
    public ResponseEntity<FloodStationsResponseDTO> getPersonsByStations(
            @RequestParam("stations") List<Integer> stations) {

        logger.info("[CALL] GET flood/station?stations={}", stations);

        List<HouseholdProfile> households = floodProfileService.getHouseholdsByStations(stations);

        List<FloodStationHouseholdDTO> householdDTOs = households.stream()
                .map(household -> {
                    List<FloodResidentDTO> residents = household.getResidents().stream()
                            .map(profile -> new FloodResidentDTO(
                                    profile.getPerson().getFirstName(),
                                    profile.getPerson().getPhone(),
                                    profile.getAge(),
                                    new MedicalHistoryDTO(
                                            profile.getMedications(),
                                            profile.getAllergies())))
                            .toList();
                    return new FloodStationHouseholdDTO(household.getAddress(), residents);
                })
                .toList();

        FloodStationsResponseDTO response = new FloodStationsResponseDTO(householdDTOs);

        logger.info("[RESPONSE] GET flood/station?stations={} -> SUCCESS", stations);
        return ResponseEntity.ok(response);
    }
}
