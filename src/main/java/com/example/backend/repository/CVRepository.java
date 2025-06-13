package com.example.backend.repository;

import com.example.backend.model.CV;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUser(User user);
    List<CV> findByUserOrderByUpdatedAtDesc(User user);
    CV findFirstByUserOrderByUpdatedAtDesc(User user);
    List<CV> findByTitleContainingIgnoreCase(String title);
    long countByUser(User user);
}