package com.example.bank.service;

import com.example.bank.dto.ClientDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.ClientMapper;
import com.example.bank.model.Client;
import com.example.bank.model.ClientType;
import com.example.bank.model.Employee;
import com.example.bank.model.IndividualClient;
import com.example.bank.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CurrentUserService currentUserService;
    @Spy
    private ClientMapper clientMapper = new ClientMapper();

    @InjectMocks
    private ClientService clientService;

    @Test
    void createIndividualClientSuccess() {
        ClientDto dto = new ClientDto();
        dto.setType(ClientType.INDIVIDUAL);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setEgn("1234567890");

        Employee creator = new Employee();
        creator.setUsername("admin");
        creator.setDisplayName("Admin");
        when(currentUserService.getCurrentEmployee()).thenReturn(Optional.of(creator));
        when(clientRepository.findByEgn("1234567890")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            c.setId(7L);
            return c;
        });

        ClientDto out = clientService.create(dto);

        assertEquals(7L, out.getId());
        assertEquals("Ivan", out.getFirstName());
        assertEquals("Admin", out.getCreatedByDisplayName());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createIndividualDuplicateEgnThrows() {
        ClientDto dto = new ClientDto();
        dto.setType(ClientType.INDIVIDUAL);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setEgn("1234567890");
        IndividualClient existing = new IndividualClient();
        existing.setId(99L);
        existing.setType(ClientType.INDIVIDUAL);
        existing.setEgn("1234567890");
        when(clientRepository.findByEgn("1234567890")).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> clientService.create(dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void createCompanyWithoutRequiredFieldsThrows() {
        ClientDto dto = new ClientDto();
        dto.setType(ClientType.COMPANY);
        dto.setCompanyName("ACME");

        assertThrows(BusinessException.class, () -> clientService.create(dto));
    }

    @Test
    void deleteWithAccountsThrows() {
        IndividualClient c = new IndividualClient();
        c.setId(1L);
        c.setType(ClientType.INDIVIDUAL);
        c.getAccounts().add(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThrows(BusinessException.class, () -> clientService.delete(1L));
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void updateChangesData() {
        IndividualClient existing = new IndividualClient();
        existing.setId(10L);
        existing.setType(ClientType.INDIVIDUAL);
        when(clientRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(clientRepository.findByEgn("1111111111")).thenReturn(Optional.empty());

        ClientDto dto = new ClientDto();
        dto.setType(ClientType.INDIVIDUAL);
        dto.setFirstName("Petar");
        dto.setLastName("Petrov");
        dto.setEgn("1111111111");

        ClientDto out = clientService.update(10L, dto);

        assertEquals("Petar", out.getFirstName());
        assertEquals("Petrov", out.getLastName());
        assertEquals("1111111111", out.getEgn());
    }

    @Test
    void getByIdNotFoundThrows() {
        when(clientRepository.findById(55L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.getById(55L));
    }

    @Test
    void getAllMapsCreatorFallbackToUsername() {
        Employee creator = new Employee();
        creator.setUsername("user1");
        creator.setDisplayName(" ");

        IndividualClient client = new IndividualClient();
        client.setId(2L);
        client.setType(ClientType.INDIVIDUAL);
        client.setFirstName("A");
        client.setLastName("B");
        client.setEgn("0123456789");
        client.setCreatedBy(creator);

        when(clientRepository.findAllWithCreatedBy()).thenReturn(List.of(client));

        List<ClientDto> out = clientService.getAll();

        assertEquals(1, out.size());
        assertEquals("user1", out.getFirst().getCreatedByDisplayName());
    }
}
