package com.visible.thread.demo.service;

import com.mongodb.DBObject;
import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.exception.VTDocNotFoundException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.FileAlreadyExistsException;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class FileStorageService implements IFileStorageService {

    private final ReactiveGridFsTemplate reactiveGridFsTemplate;

    public FileStorageService(final ReactiveGridFsTemplate reactiveGridFsTemplate) {
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
    }

    @Override
    public Flux<DataBuffer> findById(final String fileId) {
       Flux<DataBuffer> dataBufferFlux =  this.reactiveGridFsTemplate.findOne(query(where("id").is(fileId)))
                .flatMap(reactiveGridFsTemplate::getResource)
               .flatMapMany(r -> r.getDownloadStream());
       return dataBufferFlux;
/*
        return DataBufferUtils.join(dataBufferFlux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
        */
    }
    @Override
    public Mono<String> storeFile(FilePart filePart, DBObject metaData) {
        return this.reactiveGridFsTemplate.store(filePart.content(),
                        filePart.filename(),
                        String.valueOf(filePart.headers().getContentType()),
                        metaData)
                .map(Object::toString);
    }

    @Override
    public Mono<String> updateFile(final FilePart filePart, final DBObject metaData, final String id) {
        return null;
    }


    @Override
    public Mono<Void> deleteFile(String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id " + docId + " does not exist")))
                .flatMap(savedVTDoc -> this.reactiveGridFsTemplate.delete(query(where("_id").is(docId))));
    }


}
