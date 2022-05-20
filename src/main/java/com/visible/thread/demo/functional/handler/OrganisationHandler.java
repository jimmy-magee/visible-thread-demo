package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.NewOrganisationForm;
import com.visible.thread.demo.dto.representations.OrganisationRepresentation;
import com.visible.thread.demo.exception.OrganisationNotFoundException;
import com.visible.thread.demo.model.Organisation;
import com.visible.thread.demo.repository.OrganisationRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Organisation Api Service
 */
@Slf4j
public class OrganisationHandler {

    private final OrganisationRepository organisationRepository;

    public OrganisationHandler(final OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }


    /**
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> getAllOrganisations(ServerRequest request) {

        Flux<OrganisationRepresentation> organisations = this.organisationRepository.findAll()
                .flatMap(this::toOrganisationRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(organisations, OrganisationRepresentation.class));
    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getOrganisationById(ServerRequest request) {
        String id = request.pathVariable("organisationId");
        Mono<OrganisationRepresentation> organisationMono = this.organisationRepository.findById(id)
                .flatMap(this::toOrganisationRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(organisationMono, OrganisationRepresentation.class));

    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> createOrganisation(ServerRequest request) {

        Mono<NewOrganisationForm> formMono = request.bodyToMono(NewOrganisationForm.class);

        Mono<OrganisationRepresentation> savedMono = formMono
                .flatMap(form -> {
                    Organisation organisation = Organisation.builder()
                            .name(form.getName())
                            .description(form.getDescription())
                            .createdDate(LocalDateTime.now())
                            .build();
                    return this.organisationRepository.save(organisation).flatMap(this::toOrganisationRepresentation);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, OrganisationRepresentation.class));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteOrganisation(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        return this.organisationRepository.findById(organisationId)
                .flatMap(savedOrganisation -> ServerResponse.ok().body(BodyInserters.fromPublisher(this.organisationRepository.delete(savedOrganisation), Void.class)))
                .switchIfEmpty(ServerResponse.badRequest()
                        .body(BodyInserters.fromValue(new OrganisationNotFoundException("Organisation not found"))));

    }


    /**
     *
     * @param organisation
     * @return Mono<OrganisationRepresentation>
     */
    private Mono<OrganisationRepresentation> toOrganisationRepresentation(final Organisation organisation) {

        return Mono.just(
                OrganisationRepresentation.builder()
                        .id(organisation.getId())
                        .name(organisation.getName())
                        .description(organisation.getDescription())
                        .build()
        );

    }


}

