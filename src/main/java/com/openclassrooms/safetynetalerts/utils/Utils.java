package com.openclassrooms.safetynetalerts.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;

@Configuration
public class Utils {
    // private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static int calculateAge(Person person, MedicalRecordRepository repo) {
        // logger.debug("[UTILS] Age computing for :{},{}", person.getFirstName(),
        // person.getLastName());
        Optional<MedicalRecord> record = repo.findByFirstNameAndLastName(
                person.getFirstName(),
                person.getLastName());

        String birthDate = record.stream()
                .map(MedicalRecord::getBirthdate)
                .findFirst()
                .orElse(null);

        if (birthDate == null) {
            return -1; // Age par défaut si pas de dossier médical
        }

        LocalDate LocalBirthDate = LocalDate.parse(birthDate, DATE_FORMATTER);
        return Period.between(LocalBirthDate, LocalDate.now()).getYears();
    }

}
