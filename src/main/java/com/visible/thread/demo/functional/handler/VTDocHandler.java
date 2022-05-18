package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

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

        log.info("downloading files for {} as stream", teamId);

        Flux<DataBuffer> downloadFileFlux = this.vtDocService.findByTeamId(teamId)
                .flatMap(r -> { String fileName = r.getFilename();
                    log.info("downloading {} as stream", fileName);
                    return r.getDownloadStream();});

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(downloadFileFlux, DataBuffer.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> downloadVTDocById(ServerRequest request) {

        String id = request.pathVariable("id");

        Flux<DataBuffer> downloadFileFlux = this.vtDocService.findById(id)
                .flatMapMany(r -> { String fileName = r.getFilename();
                    log.info("downloading {} as stream", fileName);
                return r.getDownloadStream();});

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(downloadFileFlux, DataBuffer.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> getVTDocByUserId(ServerRequest request) {

        String userId = request.pathVariable("userId");

        Flux<DataBuffer> downloadFileFlux = this.vtDocService.findByUserId(userId)
                .flatMap(r -> {
                    String fileName = r.getFilename();
                    log.info("downloading {} as stream", fileName);
                    return r.getDownloadStream();
                });

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(downloadFileFlux, DataBuffer.class));

    }

    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> uploadVTDocs(ServerRequest request) {

        String organisationId = request.pathVariable("organisationId");

        String teamId = request.pathVariable("teamId");

        String userId = request.pathVariable("userId");

        Flux<Part> partFlux = request.body(BodyExtractors.toParts());

        log.debug("Uploading doc for user {} in team {} in organisation {} ", userId, teamId, organisationId);

        Mono<MultiValueMap<String, Part>> multiPartFormMono = request.body(BodyExtractors.toMultipartData());

        Mono<String> vtDocIdFlux = this.vtDocService.createVTDoc(multiPartFormMono, organisationId, teamId, userId);

      return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocIdFlux, String.class));

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

