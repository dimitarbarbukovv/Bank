package com.example.bank.repository;

import com.example.bank.model.Client;
import com.example.bank.model.CompanyClient;
import com.example.bank.model.IndividualClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM IndividualClient c WHERE c.egn = :egn")
    Optional<IndividualClient> findByEgn(@Param("egn") String egn);

    @Query("SELECT c FROM CompanyClient c WHERE c.eik = :eik")
    Optional<CompanyClient> findByEik(@Param("eik") String eik);

    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.createdBy")
    List<Client> findAllWithCreatedBy();
}

