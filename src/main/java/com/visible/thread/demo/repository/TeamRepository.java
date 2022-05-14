package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.Team;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeamRepository extends ReactiveMongoRepository<Team, String> {

    public Mono<Team> findByName(final String firstName);

    public Flux<Team> findByDescription(final String lastName);

    public Flux<Team> findByOrganisationId(final String organisationId);

}
