package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataMongoTest
public class TeamRepositoryTest {

    @Autowired
    private TeamRepository repository;

    private Team teamOne = Team.builder().build();

    private Team teamTwo = Team.builder().build();

    private Team teamThree = Team.builder().build();


    @Test
    public void testShouldFindAll() {

        Publisher<Team> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("saveAllTeams")
                        .thenMany(this.repository.saveAll(Flux.just(this.teamOne, this.teamTwo, this.teamThree)));

        Publisher<Team> find = this.repository.findAll();

        Publisher<Team> composite = Flux
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testShouldSaveFind() {

        Team team = Team.builder()
                .id("3")
                .build();


        Publisher<Team> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("deleteAllTeams")
                        .thenMany(this.repository.saveAll(Flux.just(team)));

        StepVerifier
                .create(setup)
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a team has state")
                                .isNotNull()
                                .hasFieldOrPropertyWithValue("id", "3")

                )
                .verifyComplete();
    }

    @Test
    public void testShouldStream() {
        Team team = Team.builder()
                .id("3")
                .organisationId("123")
                .name("Engineering Team")
                .description("VT Technical Engineering team.")
                .createdDate(LocalDateTime.now().minusDays(1))
                .modificationDate(LocalDateTime.now())
                .build();

        Publisher<Team> teamFlux = Flux
                .just(team)
                .repeat(1);

        StepVerifier
                .create(teamFlux)
                .expectSubscription()
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a team has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a team has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .expectComplete()
                .verify();

    }


}
