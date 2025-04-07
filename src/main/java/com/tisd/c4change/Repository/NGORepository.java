package com.tisd.c4change.Repository;

import com.tisd.c4change.Entity.NGOProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@Repository
public interface NGORepository extends JpaRepository<NGOProfile, Long> {
    Optional<NGOProfile> findNGOUserById(Long id);
    Optional<NGOProfile> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT n FROM NGOProfile n WHERE " +
            "LOWER(n.orgName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(n.orgMission) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<NGOProfile> searchNGOs(@Param("query") String query);

    boolean existsByRegNumber(@NotBlank String regNumber);
}
