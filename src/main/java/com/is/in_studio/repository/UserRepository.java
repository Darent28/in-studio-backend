package com.is.in_studio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash WHERE u.userId = :id")
    void updatePassword(@Param("id") Long id, @Param("hash") String hash);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email)     LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchByNameOrEmail(@Param("q") String q);

    List<User> findByRole(User.UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findRecentByRole(@Param("role") User.UserRole role, Pageable pageable);
}
