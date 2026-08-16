package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.ChecklistTemplateService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.ChecklistItemTemplateDTO;
import com.tradingjournal.presentation.dto.CreateChecklistItemTemplateRequest;
import com.tradingjournal.presentation.dto.UpdateChecklistItemTemplateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checklist-templates")
public class ChecklistTemplateController {

    private final ChecklistTemplateService service;

    public ChecklistTemplateController(ChecklistTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ChecklistItemTemplateDTO>> getTemplates(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getTemplates(user));
    }

    @PostMapping
    public ResponseEntity<ChecklistItemTemplateDTO> createTemplate(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateChecklistItemTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTemplate(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChecklistItemTemplateDTO> updateTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChecklistItemTemplateRequest request) {
        return ResponseEntity.ok(service.updateTemplate(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        service.deactivateTemplate(user, id);
        return ResponseEntity.noContent().build();
    }
}
