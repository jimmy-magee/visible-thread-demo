package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.Organisation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganisationRepository extends ReactiveMongoRepository<Organisation, String> {

    public Mono<Organisation> findByName(final String firstName);

    public Flux<Organisation> findByDescription(final String lastName);

}
