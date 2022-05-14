package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.Organisation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataMongoTest
public class OrganisationRepositoryTest {

    @Autowired
    private OrganisationRepository repository;

    private Organisation organisationOne = Organisation.builder().build();

    private Organisation organisationTwo = Organisation.builder().build();

    private Organisation organisationThree = Organisation.builder().build();


    @Test
    public void testShouldFindAll() {

        Publisher<Organisation> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("saveAllOrganisations")
                        .thenMany(this.repository.saveAll(Flux.just(this.organisationOne, this.organisationTwo, this.organisationThree)));

        Publisher<Organisation> find = this.repository.findAll();

        Publisher<Organisation> composite = Flux
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testShouldSaveFind() {

        Organisation organisation = Organisation.builder()
                .id("3")
                .build();


        Publisher<Organisation> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("deleteAllOrganisations")
                        .thenMany(this.repository.saveAll(Flux.just(organisation)));

        StepVerifier
                .create(setup)
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a organisation has state")
                                .isNotNull()
                                .hasFieldOrPropertyWithValue("id", "3")

                )
                .verifyComplete();
    }

    @Test
    public void testShouldStream() {
        Organisation organisation = Organisation.builder()
                .id("3")
                .name("Engineering Organisation")
                .description("VT Technical Engineering organisation.")
                .createdDate(LocalDateTime.now().minusDays(1))
                .modificationDate(LocalDateTime.now())
                .build();

        Publisher<Organisation> organisationFlux = Flux
                .just(organisation)
                .repeat(1);

        StepVerifier
                .create(organisationFlux)
                .expectSubscription()
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a organisation has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a organisation has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .expectComplete()
                .verify();

    }


}

