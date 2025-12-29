package com.openclassrooms.safetynetalerts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Firestation {

    private String address;
    private int station;

}
