package com.is.in_studio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.is.in_studio.entity.UserPaymentMethod;

public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Long> {
    List<UserPaymentMethod> findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    boolean existsByUser_UserIdAndStripePaymentMethodId(Long userId, String stripePaymentMethodId);
}
