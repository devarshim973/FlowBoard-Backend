package com.flowboard.auth_service.repository;

import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);

    public boolean existsByEmail(String email);

    public Page<User> findAllByRole(ROLE role, Pageable pageable);

    public Page<User> searchByFullName(String fullName, Pageable pageable);

}
