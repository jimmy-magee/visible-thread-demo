package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.NewUserForm;
import com.visible.thread.demo.dto.forms.UpdateUserForm;
import com.visible.thread.demo.dto.forms.UserCreationQueryForm;
import com.visible.thread.demo.dto.representations.UserRepresentation;
import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.exception.UserNotFoundException;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.model.User;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.service.IVTDocService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.*;
import java.util.List;

/**
 * User Api Service
 */
@Slf4j
public class UserHandler {

    private final IVTDocService vtDocService;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;


    /**
     *
     * @param vtDocService
     * @param userRepository
     * @param teamRepository
     */
    public UserHandler(final IVTDocService vtDocService, final UserRepository userRepository, final TeamRepository teamRepository) {
        this.vtDocService = vtDocService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getAllUsersByOrganisationId(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        log.debug("Looking up users by organisation id {}", organisationId);

        Flux<UserRepresentation> userFlux = this.userRepository.findByOrganisationId(organisationId)
                .switchIfEmpty(Flux.empty())
                .flatMap(this::toUserRepresentation);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(userFlux, UserRepresentation.class));

    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getUserById(ServerRequest request) {

        String id = request.pathVariable("userId");
        log.debug("Looking up users by user id {}", id);

        Mono<UserRepresentation> userMono = this.userRepository.findById(id)
                .flatMap(this::toUserRepresentation);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(userMono, UserRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getUserByEmail(ServerRequest request) {

        String email = request.pathVariable("email_address");
        log.debug("Looking up user by email {}", email);

        Mono<UserRepresentation> userMono = this.userRepository.findByEmail(email)
                .flatMap(this::toUserRepresentation);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(userMono, UserRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");
        String teamId = request.pathVariable("teamId");

        log.debug("Creating new user for organisation {} and assigning to team {}", organisationId, teamId);

        Mono<NewUserForm> formMono = request.bodyToMono(NewUserForm.class);
        Mono<Team> teamMono = this.teamRepository.findById(teamId);

        Mono<UserRepresentation> savedUserMono = formMono
                .map(form -> User.builder()
                        .firstname(form.getFirstName())
                        .lastname(form.getLastName())
                        .organisationId(organisationId)
                        .email(form.getEmail())
                        .phone(form.getPhone())
                        .isEmailVerified(false)
                        .createdDate(LocalDateTime.now())
                        .build())
                .flatMap(userRepository::save)
                .flatMap(this::toUserRepresentation);

        return Mono.zip(teamMono, savedUserMono)
                .flatMap((Tuple2<Team, UserRepresentation> data) -> {

                    Team team = data.getT1();
                    UserRepresentation user = data.getT2();

                    if (!team.getUsers().contains(user.getId())) {
                        team.getUsers().add(user.getId());
                    }

                    return this.teamRepository.save(team)
                            .flatMap(updatedTeam -> ServerResponse.ok()
                                    .contentType(APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(user)));
                });

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> updateUser(ServerRequest request) {

        String id = request.pathVariable("userId");
        Mono<UpdateUserForm> formMono = request.bodyToMono(UpdateUserForm.class);
        log.debug("Updating user {}", id);
        Mono<UserRepresentation> savedMono = formMono
                .flatMap(form -> {
                    return userRepository.findById(id)
                            .switchIfEmpty(Mono.error(new UserNotFoundException("User with id " + id + " does not exist")))
                            .flatMap(user -> {
                                user.setFirstname(form.getFirstName());
                                user.setLastname(form.getLastName());
                                user.setEmail(form.getEmail());
                                user.setPhone(form.getPhone());
                                user.setModificationDate(LocalDateTime.now());
                                return this.userRepository.save(user);
                            }).flatMap(this::toUserRepresentation);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, UserRepresentation.class));
    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> findUsersCreatedInDateRange(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        Mono<UserCreationQueryForm> formMono = request.bodyToMono(UserCreationQueryForm.class);

        Flux<UserRepresentation> userFlux = formMono
                .flatMap(form -> {
                    log.debug("Looking up users created between {} and {}", form.getStartDate(), form.getEndDate());
                    LocalDate startDate = LocalDate.parse(form.getStartDate());
                    LocalDate endDate = LocalDate.parse(form.getEndDate());

                    return findUsersCreatedInDateRange(organisationId, startDate, endDate)
                            .flatMap(this::toUserRepresentation)
                            .collectList();
                }).flatMapMany(Flux::fromIterable);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(userFlux, UserRepresentation.class));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> findInActiveUsersInDateRange(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        Mono<UserCreationQueryForm> formMono = request.bodyToMono(UserCreationQueryForm.class);

       return formMono
                .flatMap(form -> {

                    LocalDate startDate = LocalDate.parse(form.getStartDate());
                    LocalDate endDate = LocalDate.parse(form.getEndDate());

                    Flux<String> createdUserIdFlux = this.userRepository.findByOrganisationId(organisationId).map(User::getId)
                            .doOnNext(s -> log.debug("Found user (created) with id {}", s));

                    Flux<VTDocRepresentation> reactiveGridFsResourceFlux = this.vtDocService.findDocsByDateRange(organisationId, form.getStartDate(), form.getStartDate());

                    Flux<String> activeUserIdFlux = reactiveGridFsResourceFlux.map(VTDocRepresentation::getUserId).cache();

                    Flux<String> filteredFlux = createdUserIdFlux.filterWhen(createdUserId -> activeUserIdFlux.hasElement(createdUserId)
                                    .map(hasElement -> !hasElement))
                            .doOnNext(s -> log.debug("Found user (inactive) with id {}", s));

                    return filteredFlux.collectList()
                            .flatMap( u -> ServerResponse.ok()
                            .contentType(APPLICATION_JSON)
                            .body(BodyInserters.fromValue(u)));
                });
    }

    /**
     *
     * @param organisationId
     * @param startDate
     * @param endDate
     * @return
     */
    private Flux<User> findUsersCreatedInDateRange(final String organisationId, final LocalDate startDate, final LocalDate endDate) {
        return userRepository.findByOrganisationId(organisationId)
                .filter(u -> u.getCreatedDate().isAfter(startDate.atStartOfDay())
                        && u.getCreatedDate().isBefore(endDate.atTime(LocalTime.MAX)));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteUser(ServerRequest request) {

        String userId = request.pathVariable("userId");

        Flux<Team> userTeamsFlux = this.teamRepository.findAll().filter(team -> team.getUsers().contains(userId));

        Mono<List<Team>> updatedTeamsMono = userTeamsFlux.flatMap(team -> {
            team.getUsers().remove(userId);
            return this.teamRepository.save(team);
        }).collectList();

        return updatedTeamsMono.flatMap(list -> {
            return this.userRepository.findById(userId)
                    .flatMap(savedUser -> ServerResponse.ok()
                            .body(BodyInserters.fromPublisher(this.userRepository.delete(savedUser), Void.class)))
                    .switchIfEmpty(ServerResponse.badRequest()
                            .body(BodyInserters.fromValue(new UserNotFoundException("User not found"))));
        });
    }


    /**
     *
     * @param user
     * @return
     */
    private Mono<UserRepresentation> toUserRepresentation(final User user) {

        return Mono.just(
                UserRepresentation.builder()
                        .id(user.getId())
                        .organisationId(user.getOrganisationId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .isEmailVerified(user.getIsEmailVerified())
                        .build()
        );

    }


}
