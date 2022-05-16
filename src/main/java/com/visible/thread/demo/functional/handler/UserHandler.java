package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.NewUserForm;
import com.visible.thread.demo.dto.forms.UpdateUserForm;
import com.visible.thread.demo.dto.representations.UserRepresentation;
import com.visible.thread.demo.exception.TeamNotFoundException;
import com.visible.thread.demo.exception.UserNotFoundException;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.model.User;
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

/**
 * User Api Service
 */
@Slf4j
public class UserHandler {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;


    public UserHandler(final UserRepository userRepository, final TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        Flux<UserRepresentation> userFlux = this.userRepository.findAll()
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
        String email = request.pathVariable("email");
        Mono<User> userMono = this.userRepository.findByEmail(email);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(userMono, User.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {

        Mono<NewUserForm> formMono = request.bodyToMono(NewUserForm.class);

        Mono<User> savedUserMono = formMono
                .map(form -> User.builder()
                                    .firstname(form.getFirstName())
                                    .lastname(form.getLastName())
                                    .email(form.getEmail())
                                    .phone(form.getPhone())
                                    .isEmailVerified(false)
                                    .createdDate(LocalDateTime.now())
                                    .build())
                            .flatMap(userRepository::save);

        Mono<Team> updatedTeam = savedUserMono.zipWith(formMono)
                .flatMap((Tuple2<User, NewUserForm> data) -> {
                    String userId = data.getT1().getId();
                    String teamId = data.getT2().getTeamId();

                    return this.teamRepository.findById(teamId).flatMap(t -> {
                        t.getUsers().add(userId);
                        return this.teamRepository.save(t);
                    });

                });


        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedUserMono, User.class));
    }


    public Mono<ServerResponse> updateUser(ServerRequest request) {

        String id = request.pathVariable("userId");
        Mono<UpdateUserForm> formMono = request.bodyToMono(UpdateUserForm.class);
        log.debug("Updating user {}", id);
        Mono<User> savedMono = formMono
                .flatMap(form -> {
                    return  userRepository.findById(id)
                            .switchIfEmpty(Mono.error(new UserNotFoundException("User with id "+id+" does not exist")))
                            .flatMap( user -> {
                                user.setFirstname(form.getFirstName());
                                user.setLastname(form.getLastName());
                                user.setEmail(form.getEmail());
                                user.setPhone(form.getPhone());
                                user.setModificationDate(LocalDateTime.now());
                                return this.userRepository.save(user);
                            });

                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, User.class));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteUser(ServerRequest request) {

        String userId = request.pathVariable("userId");

        return this.userRepository.findById(userId)
                .flatMap(savedUser -> ServerResponse.ok().body(BodyInserters.fromPublisher(this.userRepository.delete(savedUser), Void.class)))
                .switchIfEmpty(ServerResponse.badRequest()
                        .body(BodyInserters.fromValue(new UserNotFoundException("User not found"))));
    }


    private Mono<UserRepresentation> toUserRepresentation(final User user) {

        return Mono.just(
                UserRepresentation.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .isEmailVerified(user.getIsEmailVerified())
                        .build()
        );

    }





}
