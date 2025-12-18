package com.openclassrooms.safetynetalerts.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;

@Configuration
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static int calculateAge(Person person, List<MedicalRecord> medicalRecords) {
        // logger.debug("[UTILS] Age computing for :{},{}", person.getFirstName(),
        // person.getLastName());
        MedicalRecord medicalRecord = medicalRecords.stream()
                .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                        && mr.getLastName().equals(person.getLastName()))
                .findFirst()
                .orElse(null);

        if (medicalRecord == null || medicalRecord.getBirthdate() == null) {
            return 0; // Age par défaut si pas de dossier médical
        }

        LocalDate birthDate = LocalDate.parse(medicalRecord.getBirthdate(), DATE_FORMATTER);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

}
