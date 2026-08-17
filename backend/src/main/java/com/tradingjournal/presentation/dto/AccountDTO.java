package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.Account;

import java.util.UUID;

public record AccountDTO(
        UUID id,
        String name,
        boolean isDefault
) {
    public static AccountDTO fromEntity(Account account) {
        return new AccountDTO(account.getId(), account.getName(), account.isDefaultAccount());
    }
}
