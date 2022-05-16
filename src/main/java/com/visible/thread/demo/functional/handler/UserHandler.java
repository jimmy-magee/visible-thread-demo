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

        String teamId = request.pathVariable("teamId");
        Mono<NewUserForm> formMono = request.bodyToMono(NewUserForm.class);

        Mono<User> savedMono = formMono
                .flatMap(form -> {
                    Mono<Team> teamMono = this.teamRepository.findById(teamId);
                    return teamMono.switchIfEmpty(Mono.error(new TeamNotFoundException("Error creating user, the Team does not exist")))
                            .map(org -> User.builder()
                                    .firstname(form.getFirstName())
                                    .lastname(form.getLastName())
                                    .email(form.getEmail())
                                    .phone(form.getPhone())
                                    .isEmailVerified(false)
                                    .createdDate(LocalDateTime.now())
                                    .build()).flatMap(userRepository::save);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, User.class));
    }


    public Mono<ServerResponse> updateUser(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
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
