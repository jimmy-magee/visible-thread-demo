package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.NewTeamForm;
import com.visible.thread.demo.dto.forms.UpdateTeamForm;
import com.visible.thread.demo.dto.representations.TeamRepresentation;
import com.visible.thread.demo.exception.OrganisationNotFoundException;
import com.visible.thread.demo.exception.TeamNotFoundException;
import com.visible.thread.demo.exception.UserNotFoundException;
import com.visible.thread.demo.model.Organisation;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.model.User;
import com.visible.thread.demo.repository.OrganisationRepository;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.List;

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

    public Mono<ServerResponse> getTeamUsers(ServerRequest request) {
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
                .flatMap(this::toTeamRepresentation);
        ;
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

        log.debug("Creating new team for organisation {}", organisationId);

        Mono<TeamRepresentation> savedMono = formMono
                .flatMap(form -> {
                    Mono<Organisation> organisationMono = this.organisationRepository
                            .findById(organisationId);
                    return organisationMono
                            .switchIfEmpty(Mono.error(new OrganisationNotFoundException("Error creating team, the Organisation with id "+organisationId+" does not exist")))
                            .map(org -> Team.builder()
                                    .organisationId(organisationId)
                                    .name(form.getName())
                                    .description(form.getDescription())
                                    .createdDate(LocalDateTime.now())
                                    .build());
                }).flatMap(teamRepository::save)
                .flatMap(this::toTeamRepresentation);
        ;
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, TeamRepresentation.class));
    }


    public Mono<ServerResponse> updateTeam(ServerRequest request) {

        String id = request.pathVariable("teamId");
        Mono<UpdateTeamForm> formMono = request.bodyToMono(UpdateTeamForm.class);
        log.debug("Updating team {}", id);
        Mono<TeamRepresentation> savedMono = formMono
                .flatMap(form -> {
                    return teamRepository.findById(id)
                            .switchIfEmpty(Mono.error(new TeamNotFoundException("Team with id " + id + " does not exist")))
                            .flatMap(team -> {
                                team.setOrganisationId(form.getOrganisationId());
                                team.setName(form.getName());
                                team.setDescription(form.getDescription());
                                team.setModificationDate(LocalDateTime.now());
                                return this.teamRepository.save(team);
                            }).flatMap(this::toTeamRepresentation);

                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, TeamRepresentation.class));

    }

    public Mono<ServerResponse> addUserToTeam(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
        String userId = request.pathVariable("userId");

        log.debug("Updating team {} adding user {}", teamId, userId);

        Mono<Team> teamMono = teamRepository.findById(teamId)
                .switchIfEmpty(Mono.error(new TeamNotFoundException("Team with id " + userId + " not found")));

        Mono<User> userMono = userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + userId + " not found")));

        Mono<Team> updatedTeamMono = teamMono.zip(teamMono, userMono)
                .flatMap((Tuple2<Team, User> data) -> {
                    Team team = data.getT1();
                    User user = data.getT2();

                    if (!team.getUsers().contains(user.getId())) {
                        team.getUsers().add(user.getId());
                    }

                    return this.teamRepository.save(team);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(updatedTeamMono, Team.class));
    }

    public Mono<ServerResponse> removeUserFromTeam(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
        String userId = request.pathVariable("userId");

        log.debug("Updating team {} removing user {}", teamId, userId);

        Mono<Team> teamMono = teamRepository.findById(teamId)
                .switchIfEmpty(Mono.error(new TeamNotFoundException("Team with id " + userId + " not found")));

        Mono<User> userMono = userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + userId + " not found")));

        Mono<Team> updatedTeamMono = Mono.zip(teamMono, userMono)
                .flatMap((Tuple2<Team, User> data) -> {
                    Team team = data.getT1();
                    User user = data.getT2();

                    if (team.getUsers().contains(user.getId())) {
                        team.getUsers().remove(user.getId());
                    }

                    return this.teamRepository.save(team);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(updatedTeamMono, Team.class));
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

        Mono<List<User>> userListMono = this.userRepository.findAll()
                .filter(user -> team.getUsers().contains(user.getId()))
                .collectSortedList();

        return userListMono.map(users -> {
            return TeamRepresentation.builder()
                    .id(team.getId())
                    .organisationId(team.getOrganisationId())
                    .name(team.getName())
                    .description(team.getDescription())
                    .users(users)
                    .build();
        }).switchIfEmpty(
                Mono.just(TeamRepresentation.builder()
                        .id(team.getId())
                        .organisationId(team.getOrganisationId())
                        .name(team.getName())
                        .description(team.getDescription())
                        .build())
        );

    }

}

