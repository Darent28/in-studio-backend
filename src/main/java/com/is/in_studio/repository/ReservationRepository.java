package com.is.in_studio.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.session.sessionId = :sessionId AND r.sessionDate = :date AND r.status != 'CANCELLED'")
    Optional<Reservation> findActiveByUserAndSessionAndDate(@Param("userId") Long userId,
                                                             @Param("sessionId") Long sessionId,
                                                             @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.session.sessionId = :sessionId AND r.sessionDate = :date")
    Optional<Reservation> findAnyByUserAndSessionAndDate(@Param("userId") Long userId,
                                                          @Param("sessionId") Long sessionId,
                                                          @Param("date") LocalDate date);

    long countBySession_SessionIdAndSessionDateAndStatus(Long sessionId, LocalDate sessionDate, String status);

    List<Reservation> findBySession_SessionIdAndSessionDateAndStatusOrderByCreatedAtAsc(
        Long sessionId, LocalDate sessionDate, String status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.session s JOIN FETCH s.room JOIN FETCH s.instructor i JOIN FETCH i.user WHERE r.user.userId = :userId AND r.status != 'CANCELLED' ORDER BY r.sessionDate DESC, r.createdAt DESC")
    List<Reservation> findActiveByUser(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user WHERE r.session.sessionId = :sessionId AND r.sessionDate = :date AND r.status != 'CANCELLED' ORDER BY r.createdAt ASC")
    List<Reservation> findActiveBySessionAndDate(@Param("sessionId") Long sessionId, @Param("date") LocalDate date);

    @Query("SELECT r.user.userId, r.user.firstName, r.user.lastName, r.user.email, COUNT(r) FROM Reservation r WHERE r.status = 'RESERVED' GROUP BY r.user.userId, r.user.firstName, r.user.lastName, r.user.email ORDER BY COUNT(r) DESC")
    List<Object[]> findTopAttendees(org.springframework.data.domain.Pageable pageable);
}
