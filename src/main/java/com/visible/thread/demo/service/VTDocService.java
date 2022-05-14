package com.visible.thread.demo.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.visible.thread.demo.dto.forms.UpdateVTDocForm;
import com.visible.thread.demo.exception.VTDocNotFoundException;
import com.visible.thread.demo.model.VTDoc;
import com.visible.thread.demo.repository.TeamRepository;
import com.visible.thread.demo.repository.UserRepository;
import com.visible.thread.demo.repository.VTDocRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Slf4j
public class VTDocService implements IVTDocService {

    private final TeamRepository teamRepository;
    private  final UserRepository userRepository;
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

    public Mono<String> createVTDoc(Mono<MultiValueMap<String, Part>> formDataMono, final String teamId, final String userId) {

        DBObject metaData = new BasicDBObject();
        metaData.put("teamId", teamId);
        metaData.put("userId", userId);

        return formDataMono.flatMap( partMultiValueMap -> {

            Map<String, Part> partMap = partMultiValueMap.toSingleValueMap();
            log.info("processing multipart form {} ", partMap);
            if(partMap.containsKey("doc")) {

                FilePart filePart = (FilePart) partMap.get("doc");
                log.info("Received file part {}", filePart.filename());
                VTDocUtils utils = new VTDocUtils();
                Flux<String> lines = utils.getLines(filePart);


                if(partMap.containsKey("profile")) {
                    FormFieldPart profile = (FormFieldPart) partMap.get("profile");
                    log.info("Received Profile metadata {}", profile.value());
                    metaData.put("profile", profile.value());
                } else {
                    Mono.error(new RuntimeException("No profile specified.."));
                }

                return this.reactiveGridFsTemplate.store(filePart.content(),
                        filePart.filename(),
                        "txt",
                        metaData)
                        .map( objId -> objId.toString());

            } else {
                Mono.error(new RuntimeException("No file attached.."));
            }

         return Mono.empty();


        });


/*
        Mono<String> stringMono = multipartFlux.flatMap(parts -> {

                    Map<String, Part> partMap = parts


                    partMap.forEach((partName, value) -> {
                        log.info("Name: {}, value: {}", partName, value);
                    });

                    // Handle file
                    FilePart image = (FilePart) partMap.get("image");
                    log.info("File name: {}", image.filename());

                    // Handle profile
                    FormFieldPart profile = (FormFieldPart) partMap.get("profile");
                    return Mono.just("hello");
                });

        Flux<FilePart> filePartFlux = multipartFlux
                .filter(part -> part instanceof FilePart)
                .cast(FilePart.class);

        Flux<FormFieldPart> formFieldPartFlux = multipartFlux
                .filter(part -> part instanceof FormFieldPart)
                .cast(FormFieldPart.class);



        Document doc = new Document();

        Flux<FormFieldPart> processedFormFiledFlux = formFieldPartFlux.map( formFieldPart -> {
            if (formFieldPart.name().contentEquals("category")) {
                String category = formFieldPart.value();
                doc.put("category", category);
                log.info("processing category attribute on form, value = {}", category);
            }
            return formFieldPart;
        });

        return Flux.zip(filePartFlux.collectList(), formFieldPartFlux.collectList())
                .flatMap( (Tuple2<List<FilePart>, List<FormFieldPart>> data) -> {
                    List<FilePart> filePartsList = data.getT1();
                    List<FormFieldPart> formFieldPartList = data.getT2();

                    List<Mono<String>> ids =  filePartsList.stream()
                            .map( filePart -> {
                                log.info("Storing filePart {}", filePart.filename());
                        return this.reactiveGridFsTemplate.store(filePart.content(),
                                        filePart.filename(),
                                        "txt",
                                        doc)
                                .map( objId -> objId.toString());
                    }).collect(Collectors.toList());
                    return Flux.fromIterable(ids);
                });

               // .flatMap( filePart -> {
               //     log.info("Storing filePart {}", filePart.filename());
               //     return this.reactiveGridFsTemplate.store(filePart.content(), filePart.filename(), "txt", doc).map( objId -> objId.toString());
               // }).doOnNext(System.out::println);
/*

        doc.put("teamId", teamId);
        doc.put("userId", userId);

        Flux<String> objectIdFlux = filePartFlux
                .flatMap( filePart -> {
                    log.info("Storing filePart {}", filePart.filename());
                    return this.reactiveGridFsTemplate.store(filePart.content(), filePart.filename(), "txt", doc).map( objId -> objId.toString());
                }).doOnNext(System.out::println);


        return objectIdFlux;
*/
/*
        Flux<VTDoc> flux = Flux.zip(filePartFlux.collectList(), formFieldPartFlux.collectList())
                .map((Tuple2<List<FilePart>, List<FormFieldPart>> data) -> {
                    List<FilePart> fileParts = data.getT1();
                    List<FormFieldPart> formFieldParts = data.getT2();

                    // extract form data..
                    NewVTDocForm form = new NewVTDocForm();

                    List<VTDoc> metatdata = formFieldParts.stream().map( formFieldPart -> {
                                if (formFieldPart.name().contentEquals("teamId")) {
                                    String id = formFieldPart.value();
                                    form.setTeamId(id);
                                    log.info("processing project id attribute on form, value = {}", id);
                                }
                                return formFieldPart;
                            }).collect(Collectors.toList());

                    // store with files.
                    Flux<org.bson.types.ObjectId> = fileParts.stream()
                            .map( filePart -> {
                                return this.reactiveGridFsTemplate.store(filePart.content(), filePart.filename(), "txt");
                            });
                    return VTDoc.builder().build();
                });

        */




/*
        Flux<org.bson.types.ObjectId> flux = fileParts.flatMap( file -> {
            return this.reactiveGridFsTemplate.store(file.content(), file.filename(), "txt");
        });

        flux.subscribe(System.out::println);

/*
        Mono<Team> teamMono = this.teamRepository.findById(teamId);
        return teamMono.switchIfEmpty(Mono.error(new TeamNotFoundException("Error creating vTDoc, the Team does not exist")))
                .map(org -> VTDoc.builder()
                        //.content(form.getContent())
                        .createdDate(LocalDateTime.now())
                        .build()).flatMap(vTDocRepository::save);

 */
    }

    public Mono<VTDoc> updateVTDoc(final String id, final UpdateVTDocForm form) {
        return vTDocRepository.findById(id)
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id "+id+" does not exist")))
                .flatMap( vTDoc -> {
                    vTDoc.setTeamId(form.getTeamId());
                    vTDoc.setModificationDate(LocalDateTime.now());
                    return this.vTDocRepository.save(vTDoc);
                });
    }

    public Mono<Void> deleteVTDoc(final String id) {
        return this.vTDocRepository.findById(id)
                .switchIfEmpty(Mono.error(new VTDocNotFoundException("VTDoc with id "+id+" does not exist")))
                .flatMap(savedVTDoc -> this.vTDocRepository.delete(savedVTDoc));
    }

}
