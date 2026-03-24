package com.example.bank.dto;

import com.example.bank.model.ClientType;
import com.example.bank.validation.ValidClientByType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ValidClientByType
public class ClientDto {

    private Long id;

    @NotNull(message = "Типът на клиента е задължителен")
    private ClientType type;

    // Individual
    @Size(max = 100, message = "Името може да е до 100 символа")
    private String firstName;

    @Size(max = 100, message = "Фамилията може да е до 100 символа")
    private String lastName;

    @Size(max = 10, min = 10, message = "ЕГН трябва да е точно 10 цифри")
    private String egn;

    // Company
    @Size(max = 255, message = "Името на фирмата може да е до 255 символа")
    private String companyName;

    @Size(max = 13, min = 9, message = "ЕИК трябва да е между 9 и 13 символа")
    private String eik;

    @Size(max = 200, message = "Името на представителя може да е до 200 символа")
    private String representativeName;

    /** Попълва се от сървъра – кой служител е създал записа */
    private String createdByUsername;
    private String createdByDisplayName;
}

