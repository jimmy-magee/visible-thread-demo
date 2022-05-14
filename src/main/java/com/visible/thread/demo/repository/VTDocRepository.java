package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.VTDoc;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VTDocRepository extends ReactiveMongoRepository<VTDoc, String> {

    public Mono<VTDoc> findByName(final String name);

    public Flux<VTDoc> findByTeamId(final String teamId);

    public Flux<VTDoc> findByUserEmail(final String userEmail);

    public Flux<VTDoc> findByUserId(final String userId);

    public Flux<VTDoc> findByOrganisationId(final String organisationId);

}
