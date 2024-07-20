package life.ggumtle.token.common.repository;

import life.ggumtle.token.common.entity.Survey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SurveyRepository extends ReactiveCrudRepository<Survey, Long> {
}
