package com.example.bank.controller;

import com.example.bank.dto.ClientDto;
import com.example.bank.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    private ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ClientDto create(@Valid @RequestBody ClientDto dto) {
        return clientService.create(dto);
    }

    @GetMapping
    public List<ClientDto> getAll(@RequestParam(value = "q", required = false) String query) {
        List<ClientDto> clients = clientService.getAll();
        if (query == null || query.isBlank()) {
            return clients;
        }
        String q = query.toLowerCase();
        return clients.stream()
                .filter(c ->
                        (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(q)) ||
                        (c.getLastName() != null && c.getLastName().toLowerCase().contains(q)) ||
                        (c.getEgn() != null && c.getEgn().contains(q)) ||
                        (c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(q)) ||
                        (c.getEik() != null && c.getEik().contains(q)))
                .collect(Collectors.toList());
    }

    @PutMapping("/{clientId}")
    public ClientDto update(@PathVariable("clientId") Long clientId, @Valid @RequestBody ClientDto dto) {
        return clientService.update(clientId, dto);
    }

    @DeleteMapping("/{clientId}")
    public void delete(@PathVariable("clientId") Long clientId) {
        clientService.delete(clientId);
    }
}

