package com.example.bank.mapper;

import com.example.bank.dto.ClientDto;
import com.example.bank.model.*;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientDto dto) {
        if (dto.getType() == ClientType.INDIVIDUAL) {
            IndividualClient c = new IndividualClient();
            c.setType(ClientType.INDIVIDUAL);
            c.setFirstName(trim(dto.getFirstName()));
            c.setLastName(trim(dto.getLastName()));
            c.setEgn(trim(dto.getEgn()));
            return c;
        }
        CompanyClient c = new CompanyClient();
        c.setType(ClientType.COMPANY);
        c.setCompanyName(trim(dto.getCompanyName()));
        c.setEik(trim(dto.getEik()));
        c.setRepresentativeName(trim(dto.getRepresentativeName()));
        return c;
    }

    public void updateEntity(Client client, ClientDto dto) {
        if (client instanceof IndividualClient ind) {
            ind.setFirstName(trim(dto.getFirstName()));
            ind.setLastName(trim(dto.getLastName()));
            ind.setEgn(trim(dto.getEgn()));
            return;
        }
        if (client instanceof CompanyClient comp) {
            comp.setCompanyName(trim(dto.getCompanyName()));
            comp.setEik(trim(dto.getEik()));
            comp.setRepresentativeName(trim(dto.getRepresentativeName()));
        }
    }

    public ClientDto toDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setType(client.getType());
        if (client instanceof IndividualClient ind) {
            dto.setFirstName(ind.getFirstName());
            dto.setLastName(ind.getLastName());
            dto.setEgn(ind.getEgn());
        } else if (client instanceof CompanyClient comp) {
            dto.setCompanyName(comp.getCompanyName());
            dto.setEik(comp.getEik());
            dto.setRepresentativeName(comp.getRepresentativeName());
        }
        Employee createdBy = client.getCreatedBy();
        if (createdBy != null) {
            dto.setCreatedByUsername(createdBy.getUsername());
            dto.setCreatedByDisplayName(resolveEmployeeDisplay(createdBy));
        }
        return dto;
    }

    private static String resolveEmployeeDisplay(Employee e) {
        if (e.getDisplayName() != null && !e.getDisplayName().isBlank()) {
            return e.getDisplayName();
        }
        return e.getUsername();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
