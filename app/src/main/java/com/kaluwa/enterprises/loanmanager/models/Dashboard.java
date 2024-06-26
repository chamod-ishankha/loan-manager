package com.kaluwa.enterprises.loanmanager.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dashboard implements Serializable {
    private int id;
    private String title;
    private String subTitle;
    private String drawable; // icon name
    private String bcCode; // background color code
    private String tcCode; // title color code
    private String stcCode; // sub title color code
    private String className;
}
