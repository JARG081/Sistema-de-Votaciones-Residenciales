package appsembly.appsembly.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import appsembly.appsembly.domain.SurveyAuditLog;

@Repository
public interface SurveyAuditLogRepository extends JpaRepository<SurveyAuditLog, Long> {
    List<SurveyAuditLog> findAllBySurveyIdOrderByActionAtDesc(Long surveyId);
}