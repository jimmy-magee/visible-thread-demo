package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.UserHandler;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.service.IUserService;
import com.visible.thread.demo.service.IVTDocService;
import com.visible.thread.demo.service.UserService;
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
public class UserRouterConfig {

    @Bean
    @Autowired
    public UserHandler userHandler(final IUserService userService, final IVTDocService vtDocService, final UserRepository userRepository, final TeamRepository teamRepository) {
        return new UserHandler(userService, vtDocService, userRepository, teamRepository);
    }

    @Bean
    public RouterFunction userRoute(UserHandler userHandler) {
        return RouterFunctions
                .route(GET("/api/v1/{organisationId}/users").and(accept(APPLICATION_JSON)), userHandler::getAllUsersByOrganisationId)
                .andRoute(GET("/api/v1/{organisationId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::getUserById)
                .andRoute(GET("/api/v1/{organisationId}/users/{userId}/images/{id}").and(accept(APPLICATION_JSON)), userHandler::getUserImageById)
                .andRoute(GET("/api/v1/{organisationId}/users/email/{email_address}").and(accept(APPLICATION_JSON)), userHandler::getUserByEmail)
                .andRoute(POST("/api/v1/{organisationId}/users/query/users_created_date_range").and(accept(APPLICATION_JSON)), userHandler::findUsersCreatedInDateRange)
                .andRoute(POST("/api/v1/{organisationId}/users/query/users_inactive_date_range").and(accept(APPLICATION_JSON)), userHandler::findInActiveUsersInDateRange)
                .andRoute(POST("/api/v1/{organisationId}/teams/{teamId}/users").and(accept(MULTIPART_FORM_DATA)), userHandler::createUser)
                .andRoute(POST("/api/v1/{organisationId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::updateUser)
                .andRoute(DELETE("/api/v1/{organisationId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::deleteUser);
    }

}
