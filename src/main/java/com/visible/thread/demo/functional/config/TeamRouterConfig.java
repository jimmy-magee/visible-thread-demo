package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.TeamHandler;
import com.visible.thread.demo.repository.OrganisationRepository;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class TeamRouterConfig {

    @Bean
    @Autowired
    public TeamHandler teamHandler(final OrganisationRepository organisationRepository, final TeamRepository teamRepository, final UserRepository userRepository) {
        return new TeamHandler(organisationRepository, teamRepository, userRepository);
    }

    @Bean
    public RouterFunction teamRoute(TeamHandler teamHandler) {
        return RouterFunctions
                .route(GET("/api//v1/teams").and(accept(APPLICATION_JSON)), teamHandler::getAllTeams)
                .andRoute(GET("/api/v1/{organisationId}/teams").and(accept(APPLICATION_JSON)), teamHandler::getTeamsByOrganisationId)
                .andRoute(GET("/api/v1/{organisationId}/teams/{teamId}").and(accept(APPLICATION_JSON)), teamHandler::getTeamById)
                .andRoute(GET("/api/v1/{organisationId}/team/{name}").and(accept(APPLICATION_JSON)), teamHandler::getTeamByName)
                .andRoute(POST("/api/v1/{organisationId}/teams").and(accept(APPLICATION_JSON)), teamHandler::createTeam)
                .andRoute(POST("/api/v1/{organisationId}/teams/{teamId}").and(accept(APPLICATION_JSON)), teamHandler::updateTeam)
                .andRoute(POST("/api/v1/{organisationId}/teams/{teamId}/user/{userId}").and(accept(APPLICATION_JSON)), teamHandler::addUserToTeam)
                .andRoute(DELETE("/api/v1/{organisationId}/teams/{teamId}/user/{userId}").and(accept(APPLICATION_JSON)), teamHandler::removeUserFromTeam)
                .andRoute(DELETE("/api/v1/{organisationId}/teams/{teamId}").and(accept(APPLICATION_JSON)), teamHandler::deleteTeam);
    }

}

