package com.artemk.schooltracking.repository;

import com.artemk.schooltracking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByParent_IdOrderByUsernameAsc(Long parentId);

    Optional<User> findByIdAndParent_Id(Long id, Long parentId);
}