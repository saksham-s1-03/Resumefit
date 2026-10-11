package com.resumeai.repo;

import com.resumeai.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Analysis> findByIdAndUserId(Long id, Long userId);
}
