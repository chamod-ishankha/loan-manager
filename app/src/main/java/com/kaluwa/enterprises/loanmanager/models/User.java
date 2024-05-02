package com.kaluwa.enterprises.loanmanager.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String FirstName;
    private String LastName;
    private String Email;
    private String Mobile;
    private String Dob;
    private String Gender;
    private String title;

}
