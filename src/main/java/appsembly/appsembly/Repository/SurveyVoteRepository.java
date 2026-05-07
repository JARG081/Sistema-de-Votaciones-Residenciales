package appsembly.appsembly.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import appsembly.appsembly.domain.SurveyVote;

@Repository
public interface SurveyVoteRepository extends JpaRepository<SurveyVote, Long> {
    boolean existsBySurveyIdAndVoterCode(Long surveyId, String voterCode);
    long countBySurveyId(Long surveyId);
    List<SurveyVote> findAllBySurveyId(Long surveyId);
}