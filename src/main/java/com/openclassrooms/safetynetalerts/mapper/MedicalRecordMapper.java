package com.openclassrooms.safetynetalerts.mapper;

import org.springframework.stereotype.Component;

import com.openclassrooms.safetynetalerts.dto.medicalrecord.MedicalRecordDTO;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;

/**
 * Mapper pour la conversion entre MedicalRecord et MedicalRecordDTO.
 * Respecte le principe SRP (Single Responsibility Principle).
 */
@Component
public class MedicalRecordMapper {

    /**
     * Convertit une entité MedicalRecord en DTO.
     *
     * @param record l'entité à convertir
     * @return le DTO correspondant
     */
    public MedicalRecordDTO toDto(MedicalRecord record) {
        if (record == null) {
            return null;
        }

        return new MedicalRecordDTO(
            record.getFirstName(),
            record.getLastName(),
            record.getBirthdate(),
            record.getMedications(),
            record.getAllergies()
        );
    }

    /**
     * Convertit un DTO MedicalRecordDTO en entité MedicalRecord.
     *
     * @param dto le DTO à convertir
     * @return l'entité correspondante
     */
    public MedicalRecord toEntity(MedicalRecordDTO dto) {
        if (dto == null) {
            return null;
        }

        MedicalRecord record = new MedicalRecord();
        record.setFirstName(dto.getFirstName());
        record.setLastName(dto.getLastName());
        record.setBirthdate(dto.getBirthdate());
        record.setMedications(dto.getMedications());
        record.setAllergies(dto.getAllergies());

        return record;
    }
}
