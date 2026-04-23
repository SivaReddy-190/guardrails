package com.project.guardrails.repository;

import com.project.guardrails.model.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotRepository extends JpaRepository<Bot, Long> {
}
