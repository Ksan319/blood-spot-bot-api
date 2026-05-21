package com.pet_projects.bloodspotbotapi.repository;

import com.pet_projects.bloodspotbotapi.model.SiteError;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteErrorRepository extends JpaRepository<SiteError, Long> {

    @Query("SELECT s FROM SiteError s ORDER BY s.createdAt DESC")
    List<SiteError> findLastErrors(Pageable pageable);
}
