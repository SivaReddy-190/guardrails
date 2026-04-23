package com.project.guardrails.repository;

import com.project.guardrails.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
