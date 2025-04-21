package com.tisd.c4change.Repository;

import com.tisd.c4change.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findById(Long id);
    List<Project> findByNgoId(Long ngo_id);
    List<Project> findByTitle(String title);
    @Query("SELECT p FROM Project p JOIN p.ngo n WHERE n.orgName = :ngoName")
    List<Project> findByNgoName(@Param("ngoName") String ngoName);
}
