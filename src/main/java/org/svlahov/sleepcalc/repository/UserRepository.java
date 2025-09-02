package org.svlahov.sleepcalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.svlahov.sleepcalc.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
