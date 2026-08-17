package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.ChecklistItemTemplate;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.ChecklistItemTemplateRepository;
import com.tradingjournal.presentation.dto.ChecklistItemTemplateDTO;
import com.tradingjournal.presentation.dto.CreateChecklistItemTemplateRequest;
import com.tradingjournal.presentation.dto.UpdateChecklistItemTemplateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class ChecklistTemplateService {

    private final ChecklistItemTemplateRepository repository;

    public ChecklistTemplateService(ChecklistItemTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemTemplateDTO> getTemplates(User user) {
        return repository.findByUserOrderBySortOrderAsc(user).stream()
                .map(ChecklistItemTemplateDTO::fromEntity)
                .toList();
    }

    public ChecklistItemTemplateDTO createTemplate(User user, CreateChecklistItemTemplateRequest request) {
        List<ChecklistItemTemplate> existing = repository.findByUserOrderBySortOrderAsc(user);
        int maxOrder = existing.stream().mapToInt(ChecklistItemTemplate::getSortOrder).max().orElse(0);

        ChecklistItemTemplate template = new ChecklistItemTemplate(user, request.text().trim(),
                request.sortOrder() != null ? request.sortOrder() : maxOrder + 1);
        ChecklistItemTemplate saved = repository.save(template);
        return ChecklistItemTemplateDTO.fromEntity(saved);
    }

    public ChecklistItemTemplateDTO updateTemplate(User user, UUID id, UpdateChecklistItemTemplateRequest request) {
        ChecklistItemTemplate template = findOwnedOrThrow(user, id);
        template.setText(request.text().trim());
        if (request.sortOrder() != null) template.setSortOrder(request.sortOrder());
        if (request.active() != null) template.setActive(request.active());
        ChecklistItemTemplate saved = repository.save(template);
        return ChecklistItemTemplateDTO.fromEntity(saved);
    }

    public void deactivateTemplate(User user, UUID id) {
        ChecklistItemTemplate template = findOwnedOrThrow(user, id);
        template.setActive(false);
        repository.save(template);
    }

    private ChecklistItemTemplate findOwnedOrThrow(User user, UUID id) {
        ChecklistItemTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist template not found"));
        if (!Objects.requireNonNull(template.getUser(), "Template must have an owner").getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist template not found");
        }
        return template;
    }
}
