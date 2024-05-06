package com.kaluwa.enterprises.loanmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Count {
    private int total;
    private int pending;
    private int approved;
    private int rejected;
}
