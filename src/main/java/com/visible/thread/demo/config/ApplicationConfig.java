package com.visible.thread.demo.config;

import com.visible.thread.demo.repository.OrganisationRepository;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
public class ApplicationConfig {



    @Bean
    @Autowired
    public IFileStorageService fileStorageService(final ReactiveGridFsTemplate reactiveGridFsTemplate){
        return new FileStorageService(reactiveGridFsTemplate);
    }

    @Bean
    @Autowired
    public IUserService userService(final ReactiveGridFsTemplate reactiveGridFsTemplate, final OrganisationRepository organisationRepository, final TeamRepository teamRepository, final UserRepository userRepository) {
        return new UserService(fileStorageService(reactiveGridFsTemplate), organisationRepository, teamRepository, userRepository);
    }

    @Bean
    @Autowired
    public IVTDocService vtDocService(final ReactiveGridFsTemplate reactiveGridFsTemplate, final TeamRepository teamRepository, final UserRepository userRepository) {
        return new VTDocService(reactiveGridFsTemplate, teamRepository, userRepository);
    }


}
