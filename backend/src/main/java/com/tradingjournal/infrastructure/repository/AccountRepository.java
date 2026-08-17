package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserOrderByNameAsc(User user);

    Optional<Account> findByUserAndId(User user, UUID id);

    Optional<Account> findByUserAndDefaultAccountTrue(User user);

    long countByUser(User user);
}
