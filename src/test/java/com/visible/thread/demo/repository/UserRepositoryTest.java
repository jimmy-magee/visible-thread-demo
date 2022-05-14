package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private User ivan = User.builder().build();

    private User jerry = User.builder().build();

    private User vinc = User.builder().build();


    @Test
    public void testShouldFindAll() {

        Publisher<User> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("saveAllUsers")
                        .thenMany(this.repository.saveAll(Flux.just(this.vinc, this.ivan, this.jerry)));

        Publisher<User> find = this.repository.findAll();

        Publisher<User> composite = Flux
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testShouldSaveFind() {

        User user = User.builder()
                .id("3")
                .build();


        Publisher<User> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("deleteAllUsers")
                        .thenMany(this.repository.saveAll(Flux.just(user)));

        StepVerifier
                .create(setup)
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a user has state")
                                .isNotNull()
                                .hasFieldOrPropertyWithValue("id", "3")
                                
                )
                .verifyComplete();
    }

    @Test
    public void testShouldStream() {
        User user = User.builder()
                .id("3")
                .email("test")
                .isEmailVerified(Boolean.FALSE)
                .lastname("the builder")
                .firstname("bob")
                .organisationId("123")
                .teamId("456")
                .phone("8080028")
                .createdDate(LocalDateTime.now().minusDays(1))
                .modificationDate(LocalDateTime.now())
                .build();

        Publisher<User> userFlux = Flux
                .just(user)
                .repeat(1);

        StepVerifier
                .create(userFlux)
                .expectSubscription()
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a user has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a user has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .expectComplete()
                .verify();

    }
    

}
