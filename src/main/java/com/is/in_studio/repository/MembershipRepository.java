package com.is.in_studio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUser_UserId(Long userId);

    List<Membership> findByStatus(Membership.MembershipStatus status);

    @Modifying
    @Query(value = "UPDATE membership SET status = 'EXPIRED'::membership_status WHERE status = 'ACTIVE'::membership_status AND end_date < :today", nativeQuery = true)
    int expireOverdue(@Param("today") LocalDate today);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.status = :status ORDER BY m.creditsLeft ASC")
    List<Membership> findByStatusWithUser(@Param("status") Membership.MembershipStatus status);
}
