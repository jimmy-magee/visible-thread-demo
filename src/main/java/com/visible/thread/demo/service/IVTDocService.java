package com.visible.thread.demo.service;

import com.visible.thread.demo.dto.forms.UpdateVTDocForm;
import com.visible.thread.demo.model.VTDoc;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IVTDocService {

    public Mono<ReactiveGridFsResource> findById(final String docId);

    public Flux<ReactiveGridFsResource> findByUserId(final String userId);

    public Flux<ReactiveGridFsResource> findByTeamId(final String teamId);

    public Mono<String> createVTDoc(final Mono<MultiValueMap<String, Part>> multiPartFormMono, final String organisationId, final String teamId, final String userId);

    public Mono<VTDoc> updateVTDoc(final String id, final UpdateVTDocForm form);

    public Mono<Void> deleteVTDoc(final String id);

}
