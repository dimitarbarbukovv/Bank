package com.example.bank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "individual_clients")
@Getter
@Setter
public class IndividualClient extends Client {

    @Pattern(regexp = "^[\\p{IsCyrillic} ]+$|^[A-Za-z ]+$",
            message = "Името трябва да бъде само на кирилица или само на латиница")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Pattern(regexp = "^[\\p{IsCyrillic} ]+$|^[A-Za-z ]+$",
            message = "Фамилията трябва да бъде само на кирилица или само на латиница")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "egn", nullable = false, unique = true, length = 10)
    private String egn;
}
