package com.epam.zmushka.employee.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private String name;
    private String dob;
    private Address address;
    private String email;
    private List<String> skills;
    private int experience;
    private float rating;
    private String description;
    private boolean verified;
    private long salary;

}
