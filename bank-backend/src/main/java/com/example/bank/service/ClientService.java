package com.example.bank.service;

import com.example.bank.dto.ClientDto;
import com.example.bank.exception.BusinessException;
import com.example.bank.exception.NotFoundException;
import com.example.bank.mapper.ClientMapper;
import com.example.bank.model.Client;
import com.example.bank.model.ClientType;
import com.example.bank.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;
    private final ClientMapper clientMapper;

    @Transactional
    public ClientDto create(ClientDto dto) {
        validate(dto);
        Client client = clientMapper.toEntity(dto);
        currentUserService.getCurrentEmployee().ifPresent(client::setCreatedBy);
        client = clientRepository.save(client);
        return clientMapper.toDto(client);
    }

    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        Client existing = getById(id);
        validateForUpdate(id, dto);
        if (existing.getType() != dto.getType()) {
            throw new BusinessException("Смяна на тип клиент не се поддържа");
        }
        clientMapper.updateEntity(existing, dto);
        return clientMapper.toDto(existing);
    }

    @Transactional
    public void delete(Long id) {
        Client existing = getById(id);
        if (!existing.getAccounts().isEmpty() || !existing.getCredits().isEmpty()) {
            throw new BusinessException("Клиент с активни сметки или кредити не може да бъде изтрит");
        }
        clientRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<ClientDto> getAll() {
        return clientRepository.findAllWithCreatedBy()
                .stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Клиентът не е намерен"));
    }

    private void validate(ClientDto dto) {
        if (dto.getType() == ClientType.INDIVIDUAL) {
            if (dto.getFirstName() == null || dto.getLastName() == null || dto.getEgn() == null) {
                throw new BusinessException("За физическо лице са задължителни име, фамилия и ЕГН");
            }
            clientRepository.findByEgn(dto.getEgn()).ifPresent(c -> {
                throw new BusinessException("Вече съществува клиент с това ЕГН");
            });
        } else {
            if (dto.getCompanyName() == null || dto.getEik() == null || dto.getRepresentativeName() == null) {
                throw new BusinessException("За юридическо лице са задължителни фирма, ЕИК и представител");
            }
            clientRepository.findByEik(dto.getEik()).ifPresent(c -> {
                throw new BusinessException("Вече съществува клиент с този ЕИК");
            });
        }
    }

    private void validateForUpdate(Long id, ClientDto dto) {
        if (dto.getType() == ClientType.INDIVIDUAL) {
            if (dto.getFirstName() == null || dto.getLastName() == null || dto.getEgn() == null) {
                throw new BusinessException("За физическо лице са задължителни име, фамилия и ЕГН");
            }
            clientRepository.findByEgn(dto.getEgn())
                    .filter(c -> !c.getId().equals(id))
                    .ifPresent(c -> {
                        throw new BusinessException("Вече съществува клиент с това ЕГН");
                    });
        } else {
            if (dto.getCompanyName() == null || dto.getEik() == null || dto.getRepresentativeName() == null) {
                throw new BusinessException("За юридическо лице са задължителни фирма, ЕИК и представител");
            }
            clientRepository.findByEik(dto.getEik())
                    .filter(c -> !c.getId().equals(id))
                    .ifPresent(c -> {
                        throw new BusinessException("Вече съществува клиент с този ЕИК");
                    });
        }
    }

}

