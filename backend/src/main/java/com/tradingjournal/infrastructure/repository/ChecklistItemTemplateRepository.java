package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.ChecklistItemTemplate;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChecklistItemTemplateRepository extends JpaRepository<ChecklistItemTemplate, UUID> {
    List<ChecklistItemTemplate> findByUserOrderBySortOrderAsc(User user);
    List<ChecklistItemTemplate> findByUserAndActiveTrueOrderBySortOrderAsc(User user);
}
