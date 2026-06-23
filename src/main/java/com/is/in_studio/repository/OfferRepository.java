package com.is.in_studio.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {

    List<Offer> findByPlan_PlanId(Integer planId);

    @Query("""
        SELECT o FROM Offer o
        WHERE o.plan.planId = :planId
          AND o.active = true
          AND (o.dayOfWeek IS NULL OR o.dayOfWeek = :dayOfWeek)
          AND (o.startHour IS NULL OR o.startHour <= :now)
          AND (o.endHour IS NULL OR o.endHour > :now)
        ORDER BY o.discountPercent DESC
        """)
    List<Offer> findActiveOffersForPlan(
        @Param("planId") Integer planId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("now") LocalTime now
    );

    default Optional<Offer> findBestActiveOffer(Integer planId, Integer dayOfWeek, LocalTime now) {
        return findActiveOffersForPlan(planId, dayOfWeek, now).stream().findFirst();
    }
}
