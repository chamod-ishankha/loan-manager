package com.kaluwa.enterprises.loanmanager.models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan {
    private String loanId;
    private int loanTypeId;
    private Double loanAmount;
    private Double interestRate;
    private int terms;
    private String startDate;
    private String dueDate;
    private Double installmentAmount;
    private String fop;
    private Double additionalCharges;
    private String description;
    private String lenderInfo;
    private String contactInfo;

    private String loanTypeIcon;
    private String loanTypeName;
}
