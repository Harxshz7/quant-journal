package com.tradingjournal.config;

import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.AccountRepository;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ensures every user has at least one account. On first boot after the
 * multi-account migration it creates a "Default" account per user and
 * backfills any journal entries that have no account yet.
 */
@Component
public class DefaultAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAccountInitializer.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public DefaultAccountInitializer(
            UserRepository userRepository,
            AccountRepository accountRepository,
            JournalEntryRepository journalEntryRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (User user : userRepository.findAll()) {
            Account defaultAccount = accountRepository.findByUserAndDefaultAccountTrue(user)
                    .orElseGet(() -> createDefaultAccount(user));

            List<JournalEntry> unassigned = journalEntryRepository.findByUser(user).stream()
                    .filter(entry -> entry.getAccount() == null)
                    .toList();

            if (!unassigned.isEmpty()) {
                for (JournalEntry entry : unassigned) {
                    entry.setAccount(defaultAccount);
                    journalEntryRepository.save(entry);
                }
                log.info("Backfilled {} journal entries for user {} to account '{}'",
                        unassigned.size(), user.getEmail(), defaultAccount.getName());
            }
        }
    }

    private Account createDefaultAccount(User user) {
        Account account = new Account(user, "Default", true);
        Account saved = accountRepository.save(account);
        log.info("Created default account for user {} -> {}", user.getEmail(), saved.getId());
        return saved;
    }
}
