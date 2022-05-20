package com.visible.thread.demo.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.visible.thread.demo.dto.representations.VTDocRepresentation;
import com.visible.thread.demo.exception.VTDocNotFoundException;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Slf4j
public class VTDocService implements IVTDocService {

    private final VTDocUtils vtDocUtils;
    private final ReactiveGridFsTemplate reactiveGridFsTemplate;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public VTDocService(final VTDocUtils vtDocUtils, final ReactiveGridFsTemplate reactiveGridFsTemplate, final TeamRepository teamRepository, final UserRepository userRepository) {
        this.vtDocUtils = vtDocUtils;
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public Mono<VTDocRepresentation> findById(final String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource)
                .flatMap(this::toVTDocRepresentation);
    }

    public Flux<VTDocRepresentation> findByTeamId(final String teamId) {
        return this.reactiveGridFsTemplate.find(query(where("metadata.teamId").is(teamId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource)
                .flatMap(this::toVTDocRepresentation);
    }

    public Flux<VTDocRepresentation> findByUserId(final String userId) {
        return this.reactiveGridFsTemplate.find(query(where("metadata.userId").is(userId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource)
                .flatMap(this::toVTDocRepresentation);
    }


    public Flux<VTDocRepresentation> findDocsByDateRange(final String fromDate, final String toDate) {
        Query rangeQuery = new Query();
        Criteria criteria = new Criteria();
        criteria.where("metadata.createdDate").gt(fromDate).lt(toDate);
        rangeQuery.addCriteria(criteria);

        return this.reactiveGridFsTemplate.find(rangeQuery)
                .flatMap(reactiveGridFsTemplate::getResource).flatMap(this::toVTDocRepresentation);
    }

    public Flux<DataBuffer> getDownloadStream(final String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource)
                .flatMapMany(r -> r.getDownloadStream());
    }

    public Mono<Long> getWordOccurances(final String docId, final String searchWord) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource)
                .flatMapMany(r -> vtDocUtils.decode(r.getContent()))
                .filter( s -> s.toLowerCase().equals(searchWord.toLowerCase()))
                .count();
    }

    public Mono<String> createVTDoc(Mono<MultiValueMap<String, Part>> formDataMono, final String organisationId, final String teamId, final String userId) {

        List<String> stopWords = Stream.of("The", "Me", "I", "Of", "And", "A", "We").map(s -> s.toLowerCase()).collect(Collectors.toList());

        return formDataMono.flatMap(partMultiValueMap -> {

            Map<String, Part> partMap = partMultiValueMap.toSingleValueMap();

            log.debug("processing multipart form {} ", partMap);

            if (!partMap.containsKey("doc")) {
                Mono.error(new RuntimeException("No file attached.."));
            }
            FilePart filePart = (FilePart) partMap.get("doc");
            // form fields example
            //FormFieldPart version = (FormFieldPart) partMap.get("version");

            Flux<String> linesIncludingBlanks = vtDocUtils.getLines(filePart);
            Flux<String> lines = linesIncludingBlanks.filter(it -> StringUtils.isNotBlank(it));

            Flux<String> wordsFlux = lines.flatMapIterable(VTDocUtils::extractWords);

            Mono<Long> wordCountMono = vtDocUtils.calculateWordCount(wordsFlux);

            Flux<String> wordsLessStopWordsFlux = wordsFlux.filter( word -> !stopWords.contains(word.toLowerCase()));

            Mono<List<Map.Entry<String, Long>>> topTenWordMono = vtDocUtils.calculateWordFrequency(wordsLessStopWordsFlux, 10);

            return Mono.zip(wordCountMono, topTenWordMono)
                    .flatMap((Tuple2<Long, List<Map.Entry<String, Long>>> data) -> {

                        List<Map.Entry<String, Long>> wordFrequencyList = data.getT2();

                        List<String> topTenWords = wordFrequencyList.stream().map(entry -> entry.getKey() + " = " + entry.getValue()).collect(Collectors.toList());

                        DBObject metaData = new BasicDBObject();
                        metaData.put("organisationId", organisationId);
                        metaData.put("teamId", teamId);
                        metaData.put("userId", userId);
                        metaData.put("createdDate", LocalDateTime.now());
                        metaData.put("wordCount", data.getT1());
                        metaData.put("wordFrequency", topTenWords);

                        log.debug("Uploading doc {} with wordcount {}, and word frequency {}", filePart.filename(), data.getT1(), topTenWords);


                        return this.reactiveGridFsTemplate.store(filePart.content(),
                                        filePart.filename(),
                                        "txt",
                                        metaData)
                                .map(objId -> objId.toString());
                    });

        });
    }

    public Mono<Void> deleteVTDoc(final String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id " + docId + " does not exist")))
                .flatMap(savedVTDoc -> this.reactiveGridFsTemplate.delete(query(where("_id").is(docId))));
    }

    private Mono<VTDocRepresentation> toVTDocRepresentation(ReactiveGridFsResource gridFsResource) {
        return Mono.just(VTDocRepresentation.builder()
                .id(gridFsResource.getFileId().toString())
                .organisationId(gridFsResource.getOptions().getMetadata().get("organisationId", String.class))
                .teamId(gridFsResource.getOptions().getMetadata().get("teamId", String.class))
                .userId(gridFsResource.getOptions().getMetadata().get("userId", String.class))
                .dateUploaded(gridFsResource.getOptions().getMetadata().get("createdDate", java.util.Date.class).toString())
                .wordCount(gridFsResource.getOptions().getMetadata().get("wordCount", Long.class))
                .wordFrequency(gridFsResource.getOptions().getMetadata().get("wordFrequency", ArrayList.class))
                .fileName(gridFsResource.getFilename())
                .build());
    }

}
