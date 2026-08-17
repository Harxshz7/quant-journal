package com.tradingjournal.presentation.account;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.AccountDTO;
import com.tradingjournal.presentation.dto.CreateAccountRequest;
import com.tradingjournal.presentation.dto.UpdateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> listAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.listAccounts(user));
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        AccountDTO created = accountService.createAccount(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        return ResponseEntity.ok(accountService.updateAccount(user, id, request));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<AccountDTO> setDefault(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(accountService.setDefault(user, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        accountService.deleteAccount(user, id);
        return ResponseEntity.noContent().build();
    }
}
