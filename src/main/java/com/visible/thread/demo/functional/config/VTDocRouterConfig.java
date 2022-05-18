package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.VTDocHandler;
import com.visible.thread.demo.service.VTDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class VTDocRouterConfig {

    @Bean
    @Autowired
    public VTDocHandler vTDocHandler(final VTDocService vtDocService) {
        return new VTDocHandler(vtDocService);
    }

    @Bean
    public RouterFunction vTDocRoute(VTDocHandler vTDocHandler) {
        return RouterFunctions
                .route(GET("/api/v1/vtdocs/{organisationId}/teams/{teamId}").and(accept(APPLICATION_JSON)), vTDocHandler::getVTDocsByTeamId)
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/teams/{teamId}/users/{userId}").and(accept(APPLICATION_JSON)), vTDocHandler::getVTDocByUserId)
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/teams/{teamId}/users/{userId}/{id}").and(accept(APPLICATION_JSON)), vTDocHandler::downloadVTDocById)
                .andRoute(POST("/api/v1/vtdocs/{organisationId}/teams/{teamId}/users/{userId}").and(accept(MULTIPART_FORM_DATA)), vTDocHandler::uploadVTDocs)
                .andRoute(DELETE("/api/v1/vtdocs/{organisationId}/teams/{teamId}/users/{userId}/{id}").and(accept(APPLICATION_JSON)), vTDocHandler::deleteVTDoc);
    }

}
