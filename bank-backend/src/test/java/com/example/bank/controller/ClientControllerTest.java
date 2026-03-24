package com.example.bank.controller;

import com.example.bank.dto.ClientDto;
import com.example.bank.model.ClientType;
import com.example.bank.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    @Test
    void getAllWithoutQueryReturnsAll() {
        ClientDto c = new ClientDto();
        c.setType(ClientType.INDIVIDUAL);
        when(clientService.getAll()).thenReturn(List.of(c));

        List<ClientDto> out = clientController.getAll(null);
        assertEquals(1, out.size());
    }

    @Test
    void getAllWithQueryFiltersByName() {
        ClientDto a = new ClientDto();
        a.setFirstName("Ivan");
        ClientDto b = new ClientDto();
        b.setFirstName("Petar");
        when(clientService.getAll()).thenReturn(List.of(a, b));

        List<ClientDto> out = clientController.getAll("iva");
        assertEquals(1, out.size());
        assertEquals("Ivan", out.getFirst().getFirstName());
    }
}
