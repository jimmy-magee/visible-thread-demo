package com.visible.thread.demo.config;

import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.service.IVTDocService;
import com.visible.thread.demo.service.VTDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    @Autowired
    public IVTDocService vtDocService(final ReactiveGridFsTemplate reactiveGridFsTemplate, final TeamRepository teamRepository, final UserRepository userRepository) {
        return new VTDocService(reactiveGridFsTemplate, teamRepository, userRepository);
    }


}
