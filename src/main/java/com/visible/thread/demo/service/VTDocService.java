package com.visible.thread.demo.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.visible.thread.demo.dto.forms.UpdateVTDocForm;
import com.visible.thread.demo.exception.VTDocNotFoundException;
import com.visible.thread.demo.model.VTDoc;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.repository.VTDocRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final VTDocRepository vTDocRepository;
    private final ReactiveGridFsTemplate reactiveGridFsTemplate;

    public VTDocService(final ReactiveGridFsTemplate reactiveGridFsTemplate, final TeamRepository teamRepository, final UserRepository userRepository, final VTDocRepository vtDocRepository) {
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.vTDocRepository = vtDocRepository;
    }

    public Mono<ReactiveGridFsResource> findById(final String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource);
    }

    public Flux<ReactiveGridFsResource> findByTeamId(final String teamId) {
        return this.reactiveGridFsTemplate.find(query(where("metadata.teamId").is(teamId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource);
    }

    public Flux<ReactiveGridFsResource> findByUserId(final String userId) {
        return this.reactiveGridFsTemplate.find(query(where("metadata.userId").is(userId)))
                .log()
                .flatMap(reactiveGridFsTemplate::getResource);
    }

    public Mono<String> createVTDoc(Mono<MultiValueMap<String, Part>> formDataMono, final String organisationId, final String teamId, final String userId) {

        return formDataMono.flatMap(partMultiValueMap -> {

            Map<String, Part> partMap = partMultiValueMap.toSingleValueMap();

            log.debug("processing multipart form {} ", partMap);

            if (!partMap.containsKey("doc")) {
                Mono.error(new RuntimeException("No file attached.."));
            }
            FilePart filePart = (FilePart) partMap.get("doc");
            // form fields example
            //FormFieldPart version = (FormFieldPart) partMap.get("version");

            VTDocUtils vtDocUtils = new VTDocUtils();

            Flux<String> linesIncludingBlanks = vtDocUtils.getLines(filePart);
            Flux<String> lines = linesIncludingBlanks.filter(it -> StringUtils.isNotBlank(it));

            Flux<String> wordsFlux = lines.flatMapIterable(VTDocUtils::extractWords);

            Mono<Long> wordCountMono = vtDocUtils.calculateWordCount(wordsFlux);

            Mono<List<Map.Entry<String, Long>>> topTenWordMono = vtDocUtils.calculateWordFrequency(wordsFlux, 10);

            return Mono.zip(wordCountMono, topTenWordMono)
                    .flatMap((Tuple2<Long, List<Map.Entry<String, Long>>> data) -> {

                        List<Map.Entry<String, Long>> wordFrequencyList = data.getT2();

                        List<String> topWords = wordFrequencyList.stream().map(entry -> entry.getKey() + " = " + entry.getValue()).collect(Collectors.toList());

                        DBObject metaData = new BasicDBObject();
                        metaData.put("organisationId", organisationId);
                        metaData.put("teamId", teamId);
                        metaData.put("userId", userId);
                        metaData.put("createdDate", LocalDateTime.now());
                        metaData.put("wordCount", data.getT1());
                        metaData.put("wordFrequency", topWords);

                        log.debug("Uploading doc {} with wordcount {}, and word frequency {}", filePart.filename(), data.getT1(), topWords);


                        return this.reactiveGridFsTemplate.store(filePart.content(),
                                        filePart.filename(),
                                        "txt",
                                        metaData)
                                .map(objId -> objId.toString());
                    });

        });
    }

    public Mono<VTDoc> updateVTDoc(final String id, final UpdateVTDocForm form) {
        return vTDocRepository.findById(id)
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id " + id + " does not exist")))
                .flatMap(vTDoc -> {
                    vTDoc.setTeamId(form.getTeamId());
                    vTDoc.setModificationDate(LocalDateTime.now());
                    return this.vTDocRepository.save(vTDoc);
                });
    }

    public Mono<Void> deleteVTDoc(final String docId) {
        return this.reactiveGridFsTemplate.findOne(query(where("_id").is(docId)))
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id " + docId + " does not exist")))
                .flatMap(savedVTDoc -> this.reactiveGridFsTemplate.delete(query(where("_id").is(docId))));
    }

}
