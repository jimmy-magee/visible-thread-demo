package com.visible.thread.demo.repository;

import com.visible.thread.demo.model.VTDoc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataMongoTest
public class VTDocRepositoryTest {

    @Autowired
    private VTDocRepository repository;

    private VTDoc vTDocOne = VTDoc.builder().build();

    private VTDoc vTDocTwo = VTDoc.builder().build();

    private VTDoc vTDocThree = VTDoc.builder().build();


    @Test
    public void testShouldFindAll() {

        Publisher<VTDoc> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("saveAllVTDocs")
                        .thenMany(this.repository.saveAll(Flux.just(this.vTDocOne, this.vTDocTwo, this.vTDocThree)));

        Publisher<VTDoc> find = this.repository.findAll();

        Publisher<VTDoc> composite = Flux
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void testShouldSaveFind() {

        VTDoc vTDoc = VTDoc.builder()
                .id("3")
                .build();


        Publisher<VTDoc> setup =
                this.repository
                        .deleteAll()
                        .checkpoint("deleteAllVTDocs")
                        .thenMany(this.repository.saveAll(Flux.just(vTDoc)));

        StepVerifier
                .create(setup)
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a vTDoc has state")
                                .isNotNull()
                                .hasFieldOrPropertyWithValue("id", "3")

                )
                .verifyComplete();
    }

    @Test
    public void testShouldStream() {
        VTDoc vTDoc = VTDoc.builder()
                .id("3")
                .organisationId("123")
                .teamId("123")
                .userId("4355a")
                .userEmail("email")
                .name("Engineering VTDoc")
                .description("VT Technical Engineering vTDoc.")
                .content("content of the document")
                .createdDate(LocalDateTime.now().minusDays(1))
                .modificationDate(LocalDateTime.now())
                .build();

        Publisher<VTDoc> vTDocFlux = Flux
                .just(vTDoc)
                .repeat(1);

        StepVerifier
                .create(vTDocFlux)
                .expectSubscription()
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a vTDoc has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .assertNext(it ->
                        Assertions
                                .assertThat(it)
                                .as("a vTDoc has state")
                                .isNotNull()
                                .hasNoNullFieldsOrProperties()
                )
                .expectComplete()
                .verify();

    }


}

