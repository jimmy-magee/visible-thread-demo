package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.service.IVTDocService;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;


/**
 * VTDoc Api Service
 */
@Slf4j
public class VTDocHandler {

    private final IVTDocService vtDocService;

    public VTDocHandler(final IVTDocService vtDocService) {
        this.vtDocService = vtDocService;
    }


    public Mono<ServerResponse> getVTDocsByTeamId(ServerRequest request) {

        String teamId = request.pathVariable("teamId");

        log.debug("Get VTDocs by team id {}", teamId);

        Flux<VTDocRepresentation> vtDocRepresentationFlux = this.vtDocService.findByTeamId(teamId);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocRepresentationFlux, VTDocRepresentation.class));

    }

    public Mono<ServerResponse> getVTDocsByTeamIdAndDate(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
        String date = request.pathVariable("date");

        log.debug("Get VTDocs by team id {} and date {}", teamId, date);

        Flux<VTDocRepresentation> vtDocRepresentationFlux = this.vtDocService.findByTeamIdAndDate(teamId, date);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocRepresentationFlux, VTDocRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getVTDocById(ServerRequest request) {

        String docId = request.pathVariable("id");

        Mono<VTDocRepresentation> vtDocRepresentationFlux = this.vtDocService.findById(docId);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocRepresentationFlux, VTDocRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getVTDocsByUserId(ServerRequest request) {

        String userId = request.pathVariable("userId");

        Flux<VTDocRepresentation> vtDocRepresentationFlux = this.vtDocService.findByUserId(userId);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocRepresentationFlux, VTDocRepresentation.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> downloadVTDocContentById(ServerRequest request) {

        String id = request.pathVariable("id");

        Flux<DataBuffer> downloadFileFlux = this.vtDocService.getDownloadStream(id);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(downloadFileFlux, DataBuffer.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> uploadVTDoc(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        String teamId = request.pathVariable("teamId");

        String userId = request.pathVariable("userId");

        Flux<Part> partFlux = request.body(BodyExtractors.toParts());

        log.debug("Uploading doc for user {} in team {} in organisation {} ", userId, teamId, organisationId);

        Mono<MultiValueMap<String, Part>> multiPartFormMono = request.body(BodyExtractors.toMultipartData());

        Mono<VTDocRepresentation> vtDocRepresentationMono = this.vtDocService.createVTDoc(multiPartFormMono, organisationId, teamId, userId);

      return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocRepresentationMono, VTDocRepresentation.class));

    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteVTDoc(ServerRequest request) {

        String vTDocId = request.pathVariable("id");

        return ServerResponse.ok().body(BodyInserters.fromPublisher(this.vtDocService.deleteVTDoc(vTDocId), Void.class));
    }

}

