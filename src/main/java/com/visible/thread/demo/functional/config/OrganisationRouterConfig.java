package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.OrganisationHandler;
import com.visible.thread.demo.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class OrganisationRouterConfig {

    @Bean
    @Autowired
    public OrganisationHandler organisationHandler(final OrganisationRepository organisationRepository) {
        return new OrganisationHandler(organisationRepository);
    }

    @Bean
    public RouterFunction organisationRoute(OrganisationHandler organisationHandler) {
        return RouterFunctions
                .route(GET("/api/v1/organisations").and(accept(APPLICATION_JSON)), organisationHandler::getAllOrganisations)
                .andRoute(GET("/api/v1/organisations/{organisationId}").and(accept(APPLICATION_JSON)), organisationHandler::getOrganisationById)
                .andRoute(POST("/api/v1/{organisationId}/organisations").and(accept(APPLICATION_JSON)), organisationHandler::createOrganisation)
                .andRoute(DELETE("/api/v1/organisations/{organisationId}").and(accept(APPLICATION_JSON)), organisationHandler::deleteOrganisation);
    }

}

