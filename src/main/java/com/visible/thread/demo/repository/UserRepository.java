package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.User;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    public Flux<User> findByFirstname(final String firstName);

    public Flux<User> findByLastname(final String lastName);

    public Mono<User> findByEmail(final String email);

    public Flux<User> findByOrganisationId(final String organisationId);

}
