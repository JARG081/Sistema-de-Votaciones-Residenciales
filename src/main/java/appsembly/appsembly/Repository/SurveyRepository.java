package appsembly.appsembly.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import appsembly.appsembly.domain.Survey;
import appsembly.appsembly.domain.SurveyStatus;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findTopByStatusOrderByCreatedAtDesc(SurveyStatus status);
    Optional<Survey> findTopByOrderByCreatedAtDesc();

    // Búsqueda por título o pregunta
    @Query("SELECT s FROM Survey s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.question) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY s.createdAt DESC")
    List<Survey> searchSurveys(@Param("searchTerm") String searchTerm);

    // Filtro por estado
    @Query("SELECT s FROM Survey s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Survey> findByStatus(@Param("status") SurveyStatus status);

    // Todas las encuestas ordenadas por fecha
    @Query("SELECT s FROM Survey s ORDER BY s.createdAt DESC")
    List<Survey> findAllOrderByCreatedDesc();
}