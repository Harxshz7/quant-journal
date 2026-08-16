package com.tradingjournal.presentation.rules;

import com.tradingjournal.application.rules.RulesService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.RulesStatusDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules")
public class RulesController {

    private final RulesService rulesService;

    public RulesController(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @GetMapping("/status")
    public ResponseEntity<RulesStatusDTO> status(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rulesService.status(user));
    }
}
