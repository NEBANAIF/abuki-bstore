package com.abuki.repository;

import com.abuki.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Most recent logins first — used by the admin-only login history page
    @Query("SELECT l FROM LoginHistory l ORDER BY l.loggedInAt DESC")
    List<LoginHistory> findAllOrderedByMostRecent();

    List<LoginHistory> findByUserEmailOrderByLoggedInAtDesc(String userEmail);
}
