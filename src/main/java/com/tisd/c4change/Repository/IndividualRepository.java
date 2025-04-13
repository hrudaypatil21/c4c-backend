package com.tisd.c4change.Repository;

import com.tisd.c4change.Entity.IndividualUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndividualRepository extends JpaRepository<IndividualUser, Long> {
    Optional<IndividualUser> findIndUserById(Long id);
    Optional<IndividualUser> findByEmail(String email);
    Optional<IndividualUser> findByFirebaseUid(String FirebaseUid);
    boolean existsByEmail(String email);

    @Query("SELECT i FROM IndividualUser i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "EXISTS (SELECT s FROM i.skills s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<IndividualUser> searchIndividuals(@Param("query") String query);
}

