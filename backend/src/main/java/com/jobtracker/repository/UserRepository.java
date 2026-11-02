package com.jobtracker.repository;

import com.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 *
 * HOW SPRING DATA JPA WORKS:
 *
 * You write an INTERFACE (not a class), extend JpaRepository, and Spring
 * auto-generates the implementation at runtime. You get these methods for free:
 *   save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
 *
 * JpaRepository<User, Long> means:
 *   - Entity type: User
 *   - Primary key type: Long
 *
 * DERIVED QUERY METHODS:
 *
 * Spring Data parses method names and generates SQL from them:
 *   findByEmail(String email)
 *     → SELECT * FROM users WHERE email = ?
 *
 *   existsByEmail(String email)
 *     → SELECT COUNT(*) > 0 FROM users WHERE email = ?
 *
 * The naming follows a pattern: findBy + FieldName, existsBy + FieldName, etc.
 * Spring validates these at startup — if you misspell a field name, the app
 * won't start (fail-fast).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login: look up a user by their email address.
    // Returns Optional because the email might not exist.
    Optional<User> findByEmail(String email);

    // Used during registration: check if an email is already taken
    // before trying to INSERT (avoids a database unique constraint violation).
    boolean existsByEmail(String email);
}
