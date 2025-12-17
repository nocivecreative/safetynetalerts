package com.openclassrooms.safetynetalerts.model;

import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class Person {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zip;
    private String phone;
    private String email;

    public Person() {
    }

}
