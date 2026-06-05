package com.is.in_studio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUser_UserId(Long userId);

    List<Membership> findByStatus(Membership.MembershipStatus status);
}
