package com.openclassrooms.safetynetalerts.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;

@Configuration
public class Utils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Bean
    public int calculateAge(Person person, List<MedicalRecord> medicalRecords) {
        MedicalRecord record = medicalRecords.stream()
                .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                        && mr.getLastName().equals(person.getLastName()))
                .findFirst()
                .orElse(null);

        if (record == null || record.getBirthdate() == null) {
            return 0; // Age par défaut si pas de dossier médical
        }

        LocalDate birthDate = LocalDate.parse(record.getBirthdate(), DATE_FORMATTER);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public int calculateAge(Person person, MedicalRecord medicalRecords) {

        if (medicalRecords.getBirthdate() == null) {
            return 0; // Age par défaut si pas de dossier médical
        }

        LocalDate birthDate = LocalDate.parse(medicalRecords.getBirthdate(), DATE_FORMATTER);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
