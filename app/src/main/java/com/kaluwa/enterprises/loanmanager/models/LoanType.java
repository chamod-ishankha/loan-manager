package com.kaluwa.enterprises.loanmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanType {
    private int id;
    private String name;

    @Override
    public String toString() {
        return name; // Only return the name
    }
}
