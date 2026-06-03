package com.is.in_studio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.Instructor;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Integer> {
}
