package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.VTDocHandler;
import com.visible.thread.demo.service.VTDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.*;
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
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/teams/{teamId}/date/{date}").and(accept(APPLICATION_JSON)), vTDocHandler::getVTDocsByTeamIdAndDate)
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/users/{userId}").and(accept(APPLICATION_JSON)), vTDocHandler::getVTDocsByUserId)
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/users/{userId}/{id}").and(accept(APPLICATION_JSON)), vTDocHandler::getVTDocById)
                .andRoute(GET("/api/v1/vtdocs/{organisationId}/users/{userId}/{id}/download").and(accept(IMAGE_JPEG)), vTDocHandler::downloadVTDocContentById)
                .andRoute(POST("/api/v1/vtdocs/{organisationId}/teams/{teamId}/users/{userId}").and(accept(MULTIPART_FORM_DATA)), vTDocHandler::uploadVTDoc)
                .andRoute(DELETE("/api/v1/vtdocs/{organisationId}/users/{userId}/{id}").and(accept(APPLICATION_JSON)), vTDocHandler::deleteVTDoc);
    }

}
