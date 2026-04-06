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

    /** За ФЛ: точно 10 цифри (валидира се с анотацията върху ClientDto). */
    private String egn;

    // Company
    @Size(max = 255, message = "Името на фирмата може да е до 255 символа")
    private String companyName;

    /** За ЮЛ: точно 10 цифри — валидира се в ValidClientByType. */
    private String eik;

    @Size(max = 200, message = "Името на представителя може да е до 200 символа")
    private String representativeName;

    /** Попълва се от сървъра – кой служител е създал записа */
    private String createdByUsername;
    private String createdByDisplayName;
}

