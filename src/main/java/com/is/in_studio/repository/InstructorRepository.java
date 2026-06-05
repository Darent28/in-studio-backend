package com.is.in_studio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Instructor;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Integer> {

    @Query("SELECT i.user.userId FROM Instructor i")
    List<Long> findAllUserIds();
}
