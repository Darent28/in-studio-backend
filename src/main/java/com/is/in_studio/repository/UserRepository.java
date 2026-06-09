package com.is.in_studio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.userId = :id")
    void deactivateById(Long id);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.active = true")
    List<User> findUnverifiedActiveUsers();

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.userId = :id")
    void markEmailVerified(Long id);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email)     LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchByNameOrEmail(@org.springframework.data.repository.query.Param("q") String q);

    List<User> findByRole(User.UserRole role);
}
