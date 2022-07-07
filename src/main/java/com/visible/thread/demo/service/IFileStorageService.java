package com.visible.thread.demo.service;

import com.mongodb.DBObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.FileAlreadyExistsException;

public interface IFileStorageService {

    public Flux<DataBuffer> findById(final String fileId);
    public Mono<String> storeFile(FilePart filePart, @Nullable DBObject metaData);
    public Mono<String> updateFile(FilePart filePart, @Nullable DBObject metaData, final String id);
    public Mono<Void> deleteFile(@Nullable String filename);

}
