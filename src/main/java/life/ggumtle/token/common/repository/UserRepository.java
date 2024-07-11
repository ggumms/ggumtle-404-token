package life.ggumtle.token.common.repository;

import life.ggumtle.token.common.entity.Users;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<Users, Long> {
    Mono<Users> findByInternalId(String internalId);
}