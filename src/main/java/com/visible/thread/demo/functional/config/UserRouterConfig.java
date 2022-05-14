package com.visible.thread.demo.functional.config;

import com.visible.thread.demo.functional.handler.UserHandler;
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
public class UserRouterConfig {

    @Bean
    @Autowired
    public UserHandler userHandler(final UserRepository userRepository, final TeamRepository teamRepository) {
        return new UserHandler(userRepository, teamRepository);
    }

    @Bean
    public RouterFunction userRoute(UserHandler userHandler) {
        return RouterFunctions
                .route(GET("/api/v1/{teamId}/users").and(accept(APPLICATION_JSON)), userHandler::getUsersByTeamId)
                .andRoute(GET("/api/v1/{teamId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::getUserById)
                .andRoute(GET("/api/v1/{teamId}/users/email/{email_address}").and(accept(APPLICATION_JSON)), userHandler::getUserByEmail)
                .andRoute(POST("/api/v1/{teamId}/users").and(accept(APPLICATION_JSON)), userHandler::createUser)
                .andRoute(POST("/api/v1/{teamId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::updateUser)
                .andRoute(DELETE("/api/v1/{teamId}/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::deleteUser);
    }

}
