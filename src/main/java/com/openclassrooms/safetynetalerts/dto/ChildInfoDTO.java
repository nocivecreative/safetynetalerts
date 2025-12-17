package com.openclassrooms.safetynetalerts.dto;

import lombok.Data;

@Data
public class ChildInfoDTO {

    private String firstName;
    private String lastName;
    private int age;

    public ChildInfoDTO() {
    }

    public ChildInfoDTO(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

}
