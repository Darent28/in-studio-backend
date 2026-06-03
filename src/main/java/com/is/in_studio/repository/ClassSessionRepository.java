package com.is.in_studio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.ClassSession;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    @Query("SELECT s FROM ClassSession s JOIN FETCH s.discipline JOIN FETCH s.instructor JOIN FETCH s.room")
    List<ClassSession> findAllWithDetails();
}
