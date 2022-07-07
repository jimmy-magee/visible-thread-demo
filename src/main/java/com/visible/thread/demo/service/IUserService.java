package com.visible.thread.demo.service;

import com.visible.thread.demo.dto.representations.UserRepresentation;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserService {

    public Flux<DataBuffer> getUserImageByFileId(String id);
    public Mono<UserRepresentation> getUserById(String id);

    public Mono<UserRepresentation> createUser(final Mono<MultiValueMap<String, Part>> multiPartFormMono, final String organisationId, final String teamId);

    public Mono<UserRepresentation> updateUser(final Mono<MultiValueMap<String, Part>> multiPartFormMono, final String organisationId, final String userId);

    public Mono<Void> deleteeUser(final Mono<MultiValueMap<String, Part>> multiPartFormMono, final String organisationId, final String userId);

}
