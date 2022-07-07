package com.visible.thread.demo.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.visible.thread.demo.dto.representations.UserRepresentation;
import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.model.Team;
import com.visible.thread.demo.model.User;
import com.visible.thread.demo.repository.OrganisationRepository;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class UserService implements IUserService {

    private final OrganisationRepository organisationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    //private final IVTDocService vtDocService;
    private final IFileStorageService fileStorageService;

    public UserService(IFileStorageService fileStorageService, OrganisationRepository organisationRepository, TeamRepository teamRepository, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        //this.vtDocService = vtDocService;
        this.organisationRepository = organisationRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserRepresentation> getUserById(String id) {
        return this.userRepository.findById(id).flatMap(this::toUserRepresentation);
    }


    @Override
    public Flux<DataBuffer> getUserImageByFileId(String id) {

        log.debug("Looking up user by user id {}", id);

        return this.fileStorageService.findById(id);
    }


    private  Mono<byte[]> mergeDataBuffers(Flux<DataBuffer> dataBufferFlux) {
        return DataBufferUtils.join(dataBufferFlux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }

    @Override
    public Mono<UserRepresentation> createUser(Mono<MultiValueMap<String, Part>> multiPartFormMono, String organisationId, String teamId) {
        return multiPartFormMono.flatMap(partMultiValueMap -> {

            Map<String, Part> partMap = partMultiValueMap.toSingleValueMap();

            log.debug("processing multipart form {} ", partMap);

            if (!partMap.containsKey("image") ) {
                Mono.error(new RuntimeException("No image attached.."));
            }
            FilePart filePart = (FilePart) partMap.get("image");

            FormFieldPart firstNameFormFieldPart = (FormFieldPart) partMap.get("firstName");
            FormFieldPart lastNameFormFieldPart = (FormFieldPart) partMap.get("lastName");
            FormFieldPart emailFormFieldPart = (FormFieldPart) partMap.get("email");
            FormFieldPart phoneFormFieldPart = (FormFieldPart) partMap.get("phone");

            DBObject metaData = new BasicDBObject();
            metaData.put("organisationId", organisationId);
            metaData.put("teamId", teamId);
            metaData.put("createdDate", LocalDateTime.now());


            Mono<Team> teamMono = this.teamRepository.findById(teamId);


            Mono<String> imageUrlMono = this.fileStorageService.storeFile(filePart, metaData);


            Mono<UserRepresentation> savedUserMono = imageUrlMono
                    .map(storedImageId -> User.builder()
                            .firstname(firstNameFormFieldPart.value())
                            .lastname(lastNameFormFieldPart.value())
                            .organisationId(organisationId)
                            .email(emailFormFieldPart.value())
                            .phone(phoneFormFieldPart.value())
                            .imageId(storedImageId)
                            .isEmailVerified(false)
                            .createdDate(LocalDateTime.now())
                            .build())
                    .doOnNext(System.out::println)
                    .flatMap(userRepository::save)
                    .flatMap(this::toUserRepresentation);

            return Mono.zip(teamMono, savedUserMono)
                    .flatMap((Tuple2<Team, UserRepresentation> data) -> {

                        Team team = data.getT1();
                        UserRepresentation user = data.getT2();

                        if (!team.getUsers().contains(user.getId())) {
                            team.getUsers().add(user.getId());
                        }

                        return this.teamRepository.save(team).map( t -> user);
                    });

        });
    }


    @Override
    public Mono<UserRepresentation> updateUser(Mono<MultiValueMap<String, Part>> multiPartFormMono, String organisationId, String userId) {
        return null;
    }

    @Override
    public Mono<Void> deleteeUser(Mono<MultiValueMap<String, Part>> multiPartFormMono, String organisationId, String userId) {
        return null;
    }

    private Mono<UserRepresentation> toUserRepresentation(final User user) {

        log.debug("Building user representation {}", user);
        return Mono.just(
                UserRepresentation.builder()
                        .id(user.getId())
                        .organisationId(user.getOrganisationId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .email(user.getEmail())
                        .isEmailVerified(user.getIsEmailVerified())
                        .imageId(user.getImageId())
                       // .image(user.getImage())
                        .build()
        );

    }
}
