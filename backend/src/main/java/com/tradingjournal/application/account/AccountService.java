package com.tradingjournal.application.account;

import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.AccountRepository;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.presentation.dto.AccountDTO;
import com.tradingjournal.presentation.dto.CreateAccountRequest;
import com.tradingjournal.presentation.dto.UpdateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public AccountService(AccountRepository accountRepository, JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> listAccounts(User user) {
        return accountRepository.findByUserOrderByNameAsc(user).stream()
                .map(AccountDTO::fromEntity)
                .toList();
    }

    public AccountDTO createAccount(User user, CreateAccountRequest request) {
        boolean isFirst = accountRepository.countByUser(user) == 0;
        Account account = new Account(user, request.name().trim(), isFirst);
        Account saved = accountRepository.save(account);
        return AccountDTO.fromEntity(saved);
    }

    public AccountDTO updateAccount(User user, UUID id, UpdateAccountRequest request) {
        Account account = findOwnedAccountOrThrow(user, id);
        account.setName(request.name().trim());
        return AccountDTO.fromEntity(accountRepository.save(account));
    }

    public AccountDTO setDefault(User user, UUID id) {
        Account account = findOwnedAccountOrThrow(user, id);

        List<Account> owned = accountRepository.findByUserOrderByNameAsc(user);
        for (Account other : owned) {
            if (other.isDefaultAccount() && !other.getId().equals(account.getId())) {
                other.setDefaultAccount(false);
                accountRepository.save(other);
            }
        }
        account.setDefaultAccount(true);
        return AccountDTO.fromEntity(accountRepository.save(account));
    }

    public void deleteAccount(User user, UUID id) {
        Account account = findOwnedAccountOrThrow(user, id);

        if (accountRepository.countByUser(user) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the only account. Create another account first.");
        }

        Account replacement = accountRepository.findByUserAndDefaultAccountTrue(user)
                .filter(defaultAccount -> !defaultAccount.getId().equals(account.getId()))
                .orElseGet(() -> {
                    Account promoted = accountRepository.findByUserOrderByNameAsc(user).stream()
                            .filter(a -> !a.getId().equals(account.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the only account."));
                    promoted.setDefaultAccount(true);
                    return accountRepository.save(promoted);
                });

        List<JournalEntry> entries = journalEntryRepository.findByUser(user).stream()
                .filter(entry -> account.getId().equals(entry.getAccount() != null ? entry.getAccount().getId() : null))
                .toList();
        for (JournalEntry entry : entries) {
            entry.setAccount(replacement);
            journalEntryRepository.save(entry);
        }

        accountRepository.delete(account);
    }

    public Account resolveOwnedAccount(User user, UUID accountId) {
        if (accountId == null) return null;
        return findOwnedAccountOrThrow(user, accountId);
    }

    public Account getDefaultAccount(User user) {
        return accountRepository.findByUserAndDefaultAccountTrue(user)
                .orElseGet(() -> accountRepository.findByUserOrderByNameAsc(user).stream()
                        .findFirst()
                        .map(account -> {
                            account.setDefaultAccount(true);
                            return accountRepository.save(account);
                        })
                        .orElse(null));
    }

    private Account findOwnedAccountOrThrow(User user, UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }
}
