package com.openclassrooms.safetynetalerts.mapper;

import org.springframework.stereotype.Component;

import com.openclassrooms.safetynetalerts.dto.PersonDTO;
import com.openclassrooms.safetynetalerts.model.Person;

/**
 * Mapper pour la conversion entre Person et PersonDTO.
 * Respecte le principe SRP (Single Responsibility Principle) en séparant
 * la logique de mapping de la logique métier.
 */
@Component
public class PersonMapper {

    /**
     * Convertit une entité Person en DTO.
     *
     * @param person l'entité à convertir
     * @return le DTO correspondant
     */
    public PersonDTO toDto(Person person) {
        if (person == null) {
            return null;
        }

        return new PersonDTO(
            person.getFirstName(),
            person.getLastName(),
            person.getAddress(),
            person.getCity(),
            person.getZip(),
            person.getPhone(),
            person.getEmail()
        );
    }

    /**
     * Convertit un DTO PersonDTO en entité Person.
     *
     * @param dto le DTO à convertir
     * @return l'entité correspondante
     */
    public Person toEntity(PersonDTO dto) {
        if (dto == null) {
            return null;
        }

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
}
