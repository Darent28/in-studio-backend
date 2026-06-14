package com.is.in_studio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUser_UserId(Long userId);

    List<Membership> findByStatus(Membership.MembershipStatus status);

    @Modifying
    @Query("UPDATE Membership m SET m.status = com.is.in_studio.entity.Membership.MembershipStatus.EXPIRED WHERE m.status = com.is.in_studio.entity.Membership.MembershipStatus.ACTIVE AND m.endDate < :today")
    int expireOverdue(LocalDate today);
}
