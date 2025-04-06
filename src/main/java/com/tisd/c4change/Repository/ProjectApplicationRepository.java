package com.tisd.c4change.Repository;

import com.tisd.c4change.Entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    List<ProjectApplication> findByProjectId(Long projectId);
    List<ProjectApplication> findByVolunteerId(Long volunteerId);
    List<ProjectApplication> findByProjectNgoId(Long ngoId);
    boolean existsByProjectIdAndVolunteerId(Long projectId, Long volunteerId);
}
