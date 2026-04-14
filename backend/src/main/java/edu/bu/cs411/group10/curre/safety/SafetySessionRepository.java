package edu.bu.cs411.group10.curre.safety;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SafetySessionRepository extends JpaRepository<SafetySession, Long> {
    Optional<SafetySession> findByRunIdAndActiveTrue(Long runId);
} // END OF INTERFACE SafetySessionRepository