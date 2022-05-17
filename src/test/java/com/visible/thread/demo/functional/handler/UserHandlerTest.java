package com.visible.thread.demo.functional.handler;

import com.visible.thread.demo.config.SecurityConfig;
import com.visible.thread.demo.dto.forms.NewUserForm;
import com.visible.thread.demo.dto.forms.UpdateUserForm;
import com.visible.thread.demo.dto.representations.UserRepresentation;
import com.visible.thread.demo.functional.config.UserRouterConfig;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.model.User;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {UserRouterConfig.class, SecurityConfig.class})
@WebFluxTest
public class UserHandlerTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TeamRepository teamRepository;

    @Autowired
    private WebTestClient webTestClient;
    

    @Test
    public void testCreateUser() {
        String organisationId = "org-id-123";
        String teamId = "team-id-123";
        String userId = "user-id-123";

        UserRepresentation userRepresentation = UserRepresentation.builder().id(userId).lastname("Pantic").build();

        NewUserForm requestBody = new NewUserForm();
        requestBody.setFirstName("Ned");
        requestBody.setLastName("Pantic");
        requestBody.setEmail("ned@optily.com");
        requestBody.setPhone("083 4396070");

        Team team = Team.builder()
                .id(teamId)
                .name("Engineering Team")
                .description("Eng Team Desc")
                .organisationId("123")
                .build();

        User user = User.builder()
                .id(userId)
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        Mono<Team> teamMono = Mono.just(team);

        Mono<User> userMono = Mono.just(user);

        when(teamRepository.findById(anyString())).thenReturn(teamMono);

        when(userRepository.save(any())).thenReturn(userMono);

        when(teamRepository.save(any())).thenReturn(teamMono);


        webTestClient.post().uri("/api/v1/{organisationId}/teams/{teamId}/users", organisationId, teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), NewUserForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRepresentation.class)
                .value(savedUser -> {
                    Assertions.assertThat(savedUser.getId()).isNotEmpty();
                    Assertions.assertThat(savedUser.getId()).isEqualTo(userId);
                    Assertions.assertThat(savedUser.getFirstname()).isEqualTo("Ned");
                    Assertions.assertThat(savedUser.getLastname()).isEqualTo("Pantic");
                });

    }

    @Test
    public void testUpdateUser() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";
        String userId = "user-id-123";

        UserRepresentation userRepresentation = UserRepresentation.builder().id("1").lastname("Pantic").build();

        NewUserForm requestBody = new NewUserForm();
        requestBody.setFirstName("Ned");
        requestBody.setLastName("Pantic");
        requestBody.setEmail("ned@optily.com");
        requestBody.setPhone("083 4396070");

        User user = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        Mono<UserRepresentation> userRepresentationMono = Mono.just(userRepresentation);
        Mono<User> userMono = Mono.just(user);

        when(userRepository.findById(anyString())).thenReturn(userMono);
        when(userRepository.save(any())).thenReturn(userMono);


        webTestClient.post().uri("/api/v1/{organisationId}/teams/{teamId}/users/{userId}", organisationId, teamId,  userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), UpdateUserForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRepresentation.class)
                .value(savedUser -> {
                    Assertions.assertThat(savedUser.getId()).isNotEmpty();
                    Assertions.assertThat(savedUser.getId()).isEqualTo("1");
                    Assertions.assertThat(savedUser.getFirstname()).isEqualTo("Ned");
                    Assertions.assertThat(savedUser.getLastname()).isEqualTo("Pantic");
                });

    }


    @Test
    public void testDeleteUser() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";
        String userId = "user-id-123";

        User user = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        Mono<User> userMono = Mono.just(user);

        when(userRepository.findById(anyString())).thenReturn(userMono);
        when(userRepository.delete(any())).thenReturn(Mono.empty());

        webTestClient.post().uri("/api/v1/{organisationId}/teams/{teamId}/users/{userId}", organisationId, teamId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

    }

    @Test
    public void testGetAllUsersForAnOrganisation() {

        String organisationId = "org-id-123";

        User userOne = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        User userTwo = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        User userThree = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        Flux<User> userFlux = Flux.just(userOne, userTwo, userThree);

        when(userRepository.findByOrganisationId(anyString())).thenReturn(userFlux);

        webTestClient.get().uri("/api/v1/{organisationId}/users", organisationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .value(token -> {
                    Assertions.assertThat(token.size()).isEqualTo(3);
                });

    }


    @Test
    public void testGetUserById() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";
        String userId = "user-id-123";

        User user = User.builder()
                .id("1")
                .firstname("Ned")
                .lastname("Pantic")
                .phone("083 4396070")
                .createdDate(LocalDateTime.now())
                .build();

        Mono<User> userMono = Mono.just(user);

        when(userRepository.findById(anyString())).thenReturn(userMono);

        webTestClient.get().uri("/api/v1/{organisationId}/teams/{teamId}/users/{userId}", organisationId, teamId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRepresentation.class)
                .value(userRepresentation -> {
                    Assertions.assertThat(userRepresentation.getId()).isNotEmpty();
                });

    }

}
