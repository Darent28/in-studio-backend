package com.is.in_studio.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {

    List<Offer> findByPlan_PlanId(Integer planId);

    @Query("SELECT o FROM Offer o WHERE o.plan.planId = :planId AND o.active = true ORDER BY o.discountPercent DESC")
    List<Offer> findActiveByPlanId(@Param("planId") Integer planId);

    /**
     * Returns active offers for a plan that match the date range and time window.
     * Day-of-week bitmask filtering is done in Java since JPQL has no bitwise ops.
     */
    @Query("""
        SELECT o FROM Offer o
        WHERE o.plan.planId = :planId
          AND o.active = true
          AND (o.startDate IS NULL OR o.startDate <= :date)
          AND (o.endDate   IS NULL OR o.endDate   >= :date)
          AND (o.startHour IS NULL OR o.startHour <= :time)
          AND (o.endHour   IS NULL OR o.endHour   >  :time)
        ORDER BY o.discountPercent DESC
        """)
    List<Offer> findCandidateOffers(
        @Param("planId") Integer planId,
        @Param("date") LocalDate date,
        @Param("time") LocalTime time
    );
}
