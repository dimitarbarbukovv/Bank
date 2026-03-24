package com.example.bank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "company_clients")
@Getter
@Setter
public class CompanyClient extends Client {

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "eik", nullable = false, unique = true, length = 13)
    private String eik;

    @Column(name = "representative_name", nullable = false, length = 200)
    private String representativeName;
}
