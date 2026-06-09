package com.is.in_studio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.membership m JOIN FETCH m.user JOIN FETCH p.plan ORDER BY p.createdAt DESC")
    List<Payment> findAllWithDetails();

    @Query("SELECT p FROM Payment p JOIN FETCH p.membership m JOIN FETCH m.user JOIN FETCH p.plan WHERE m.membershipId = :membershipId")
    List<Payment> findByMembershipId(@Param("membershipId") Long membershipId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.membership m JOIN FETCH m.user JOIN FETCH p.plan WHERE p.paymentId = :id")
    Optional<Payment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p.membership.membershipId, MAX(p.paymentId) FROM Payment p GROUP BY p.membership.membershipId")
    List<Object[]> findLastPaymentIdPerMembership();

    @Query("SELECT p FROM Payment p JOIN FETCH p.membership m JOIN FETCH m.user JOIN FETCH p.plan WHERE p.transactionRef = :ref")
    Optional<Payment> findByTransactionRef(@Param("ref") String transactionRef);
}
