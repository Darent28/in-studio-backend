package com.is.in_studio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.is.in_studio.entity.CouponUsage;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Integer> {
    boolean existsByCouponCouponIdAndUserUserId(Integer couponId, Long userId);
}
