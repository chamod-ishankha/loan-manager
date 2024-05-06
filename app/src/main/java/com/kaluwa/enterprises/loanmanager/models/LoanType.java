package com.kaluwa.enterprises.loanmanager.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanType implements Serializable {
    private int id;
    private String name;

    @Override
    public String toString() {
        return name; // Only return the name
    }
}
