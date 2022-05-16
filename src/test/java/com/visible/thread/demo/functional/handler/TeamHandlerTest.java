package com.visible.thread.demo.functional.handler;

import com.visible.thread.demo.config.SecurityConfig;
import com.visible.thread.demo.dto.forms.NewTeamForm;
import com.visible.thread.demo.dto.representations.TeamRepresentation;
import com.visible.thread.demo.functional.config.TeamRouterConfig;
import com.visible.thread.demo.model.Organisation;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.repository.OrganisationRepository;
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
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TeamRouterConfig.class, SecurityConfig.class})
@WebFluxTest
public class TeamHandlerTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private OrganisationRepository organisationRepository;

    @MockBean
    private TeamRepository teamRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;


    @Test
    public void testCreateTeam() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";

        TeamRepresentation teamRepresentation = TeamRepresentation.builder()
                .id(teamId)
                .organisationId(organisationId)
                .name("Engineering Team")
                .description("Eng Team Desc").build();

        NewTeamForm requestBody = new NewTeamForm();
        requestBody.setName("Engineering Team");
        requestBody.setDescription("Eng Team Desc");

        Organisation organisation = Organisation.builder().id(organisationId).build();

        Team team = Team.builder()
                .id(teamId)
                .name("Engineering Team")
                .description("Eng Team Desc")
                .organisationId(organisationId)
                .build();

        Mono<Organisation> organisationMono = Mono.just(organisation);

        Mono<Team> teamMono = Mono.just(team);

        when(organisationRepository.findById(anyString())).thenReturn(organisationMono);

        when(teamRepository.findById(anyString())).thenReturn(teamMono);

        when(teamRepository.save(any())).thenReturn(teamMono);

        when(userRepository.findAll()).thenReturn(Flux.empty());

        webTestClient.post().uri("/api/v1/{organisationId}/teams", organisationId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), NewTeamForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeamRepresentation.class)
                .value(savedTeam -> {
                    Assertions.assertThat(savedTeam.getId()).isNotEmpty();
                    Assertions.assertThat(savedTeam.getId()).isEqualTo(teamId);
                    Assertions.assertThat(savedTeam.getName()).isEqualTo("Engineering Team");
                    Assertions.assertThat(savedTeam.getDescription()).isEqualTo("Eng Team Desc");
                });

    }

    @Test
    public void testUpdateTeam() {
        String organisationId = "org-id-123";
        String teamId = "team-id-123";

        TeamRepresentation teamRepresentation = TeamRepresentation.builder()
                .id(teamId)
                .organisationId(organisationId)
                .name("Software Engineering Team")
                .description("Soft Eng Team Desc").build();

        NewTeamForm requestBody = new NewTeamForm();
        requestBody.setName("Software Engineering Team");
        requestBody.setDescription("Soft Eng Team Desc");

        Organisation organisation = Organisation.builder().id(organisationId).build();

        Team team = Team.builder()
                .id(teamId)
                .name("Engineering Team")
                .description("Eng Team Desc")
                .organisationId(organisationId)
                .build();

        Mono<Organisation> organisationMono = Mono.just(organisation);

        Mono<TeamRepresentation> teamRepresentationMono = Mono.just(teamRepresentation);
        Mono<Team> teamMono = Mono.just(team);

        when(teamRepository.findById(anyString())).thenReturn(teamMono);
        when(teamRepository.save(any())).thenReturn(teamMono);
        when(userRepository.findAll()).thenReturn(Flux.empty());

        webTestClient.post().uri("/api/v1/{teamId}/teams/{teamId}", teamId, teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), NewTeamForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeamRepresentation.class)
                .value(savedTeam -> {
                    Assertions.assertThat(savedTeam.getId()).isNotEmpty();
                    Assertions.assertThat(savedTeam.getId()).isEqualTo(teamId);
                    Assertions.assertThat(savedTeam.getName()).isEqualTo("Software Engineering Team");
                    Assertions.assertThat(savedTeam.getDescription()).isEqualTo("Soft Eng Team Desc");
                });

    }


    @Test
    public void testDeleteTeam() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";

        TeamRepresentation teamRepresentation = TeamRepresentation.builder()
                .id(teamId)
                .organisationId(organisationId)
                .name("Software Engineering Team")
                .description("Soft Eng Team Desc").build();

        NewTeamForm requestBody = new NewTeamForm();
        requestBody.setName("Software Engineering Team");
        requestBody.setDescription("Soft Eng Team Desc");

        Organisation organisation = Organisation.builder().id(organisationId).build();

        Team team = Team.builder()
                .id(teamId)
                .name("Engineering Team")
                .description("Eng Team Desc")
                .organisationId(organisationId)
                .build();

        Mono<Team> teamMono = Mono.just(team);

        when(teamRepository.findById(anyString())).thenReturn(teamMono);
        when(teamRepository.delete(any())).thenReturn(Mono.empty());

        webTestClient.post().uri("/api/v1/{organisationId}/teams/{teamId}", organisationId, teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

    }

    @Test
    public void testGetAllTeams() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";

        Team teamOne = Team.builder()
                .id("1")
                .createdDate(LocalDateTime.now())
                .build();

        Team teamTwo = Team.builder()
                .id("1")
                .createdDate(LocalDateTime.now())
                .build();

        Team teamThree = Team.builder()
                .id("1")
                .createdDate(LocalDateTime.now())
                .build();

        Flux<Team> teamFlux = Flux.just(teamOne, teamTwo, teamThree);

        when(teamRepository.findAll()).thenReturn(teamFlux);
        when(userRepository.findAll()).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/v1/teams")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .value(token -> {
                    Assertions.assertThat(token.size()).isEqualTo(3);
                });

    }


    @Test
    public void testGetTeamById() {

        String organisationId = "org-id-123";
        String teamId = "team-id-123";

        TeamRepresentation teamRepresentation = TeamRepresentation.builder()
                .id(teamId)
                .organisationId(organisationId)
                .name("Software Engineering Team")
                .description("Soft Eng Team Desc").build();

        NewTeamForm requestBody = new NewTeamForm();
        requestBody.setName("Software Engineering Team");
        requestBody.setDescription("Soft Eng Team Desc");

        Team team = Team.builder()
                .id(teamId)
                .name("Engineering Team")
                .description("Eng Team Desc")
                .organisationId(organisationId)
                .users(new HashSet<>())
                .build();


        Mono<Team> teamMono = Mono.just(team);

        when(teamRepository.findById(anyString())).thenReturn(teamMono);
        when(userRepository.findAll()).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/v1/{organisationId}/teams/{teamId}", organisationId, teamId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeamRepresentation.class)
                .value(teamRep -> {
                    Assertions.assertThat(teamRep.getId()).isNotEmpty();
                });

    }

}

