package com.openclassrooms.safetynetalerts.model;

import lombok.Data;

@Data
public class Firestation {

    private String address;
    private int station;

    public Firestation() {
    }

}
