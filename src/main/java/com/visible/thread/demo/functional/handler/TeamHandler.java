package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.NewTeamForm;
import com.visible.thread.demo.dto.forms.UpdateTeamForm;
import com.visible.thread.demo.dto.representations.TeamRepresentation;
import com.visible.thread.demo.exception.OrganisationNotFoundException;
import com.visible.thread.demo.exception.TeamNotFoundException;
import com.visible.thread.demo.model.Organisation;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.repository.OrganisationRepository;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Team Api Service
 */
@Slf4j
public class TeamHandler {

    private final OrganisationRepository organisationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;


    public TeamHandler(final OrganisationRepository organisationRepository, final TeamRepository teamRepository, final UserRepository userRepository) {

        this.organisationRepository = organisationRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getAllTeams(ServerRequest request) {

        Flux<TeamRepresentation> teamFlux = this.teamRepository.findAll()
                .flatMap(this::toTeamRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(teamFlux, TeamRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getTeamsByOrganisationId(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        Flux<TeamRepresentation> teamFlux = this.teamRepository.findByOrganisationId(organisationId)
                .flatMap(this::toTeamRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(teamFlux, TeamRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getTeamById(ServerRequest request) {
        String id = request.pathVariable("teamId");
        Mono<TeamRepresentation> teamMono = this.teamRepository.findById(id)
                .flatMap(this::toTeamRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(teamMono, TeamRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getTeamByName(ServerRequest request) {
        String name = request.pathVariable("name");
        Mono<TeamRepresentation> teamMono = this.teamRepository.findByName(name)
                .flatMap(this::toTeamRepresentation);;
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(teamMono, TeamRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> createTeam(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");
        Mono<NewTeamForm> formMono = request.bodyToMono(NewTeamForm.class);

        Mono<Team> savedMono = formMono
                .flatMap(form -> {
                    Mono<Organisation> teamMono = this.organisationRepository.findById(organisationId);
                    return teamMono.switchIfEmpty(Mono.error(new OrganisationNotFoundException("Error creating team, the Team does not exist")))
                            .map(org -> Team.builder()
                                    .organisationId(organisationId)
                                    .name(form.getName())
                                    .description(form.getDescription())
                                    .createdDate(LocalDateTime.now())
                                    .build()).flatMap(teamRepository::save);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, Team.class));
    }


    public Mono<ServerResponse> updateTeam(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
        String id = request.pathVariable("teamId");
        Mono<UpdateTeamForm> formMono = request.bodyToMono(UpdateTeamForm.class);
        log.debug("Updating team {}", id);
        Mono<Team> savedMono = formMono
                .flatMap(form -> {
                    return  teamRepository.findById(id)
                            .switchIfEmpty(Mono.error(new TeamNotFoundException("Team with id "+id+" does not exist")))
                            .flatMap( team -> {
                                team.setOrganisationId(form.getOrganisationId());
                                team.setName(form.getName());
                                team.setDescription(form.getDescription());
                                team.setModificationDate(LocalDateTime.now());
                                return this.teamRepository.save(team);
                            });

                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, Team.class));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteTeam(ServerRequest request) {

        String id = request.pathVariable("id");

        return this.teamRepository.findById(id)
                .flatMap(savedTeam -> ServerResponse.ok()
                        .body(BodyInserters.fromPublisher(this.teamRepository.delete(savedTeam), Void.class)))
                .switchIfEmpty(ServerResponse.badRequest()
                        .body(BodyInserters.fromValue(new TeamNotFoundException("Team not found"))));
    }


    private Mono<TeamRepresentation> toTeamRepresentation(final Team team) {

        return Mono.just(
                TeamRepresentation.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .description(team.getDescription())
                        .build()
        );

    }





}

