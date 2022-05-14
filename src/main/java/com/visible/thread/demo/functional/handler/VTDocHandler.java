package com.visible.thread.demo.functional.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.visible.thread.demo.dto.forms.UpdateVTDocForm;
import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.model.VTDoc;
import com.visible.thread.demo.service.IVTDocService;


import org.bson.types.ObjectId;
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

        String teamId = request.pathVariable("teamId");

        String userId = request.pathVariable("userId");

        Flux<Part> partFlux = request.body(BodyExtractors.toParts());

        Mono<MultiValueMap<String, Part>> multiPartFormMono = request.body(BodyExtractors.toMultipartData());

        Mono<String> vtDocIdFlux = this.vtDocService.createVTDoc(multiPartFormMono, teamId, userId);

      return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(vtDocIdFlux, String.class));
    }



    public Mono<ServerResponse> updateVTDoc(ServerRequest request) {

        String teamId = request.pathVariable("teamId");
        String id = request.pathVariable("vTDocId");
        Mono<UpdateVTDocForm> formMono = request.bodyToMono(UpdateVTDocForm.class);
        log.debug("Updating vTDoc {}", id);

        Mono<VTDoc> savedMono = formMono
                .flatMap(form -> {
                    return  vtDocService.updateVTDoc(teamId, form);
                });
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(savedMono, VTDoc.class));
    }


    /**
     * @param request
     * @return response
     */
    public Mono<ServerResponse> deleteVTDoc(ServerRequest request) {

        String vTDocId = request.pathVariable("id");

        return ServerResponse.ok().body(BodyInserters.fromPublisher(this.vtDocService.deleteVTDoc(vTDocId), Void.class));
    }


    private Mono<VTDocRepresentation> toVTDocRepresentation(final VTDoc vTDoc) {

        return Mono.just(
                VTDocRepresentation.builder()
                        .id(vTDoc.getId())
                        .teamId(vTDoc.getTeamId())
                        .build()
        );

    }


}

