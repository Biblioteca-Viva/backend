package org.bibliotecaviva.backend.infrastructure.persistance.repository;

import org.bibliotecaviva.backend.domain.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
