package com.visible.thread.demo.service;

import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IVTDocService {

    public Mono<VTDocRepresentation> findById(final String docId);

    public Flux<DataBuffer> getDownloadStream(final String docId);

    public Flux<VTDocRepresentation> findByUserId(final String userId);

    public Flux<VTDocRepresentation> findByTeamId(final String teamId);

    public Flux<VTDocRepresentation> findDocsByDateRange(final String fromDate, final String toDate);

    public Mono<String> createVTDoc(final Mono<MultiValueMap<String, Part>> multiPartFormMono, final String organisationId, final String teamId, final String userId);

    public Mono<Void> deleteVTDoc(final String id);

}
