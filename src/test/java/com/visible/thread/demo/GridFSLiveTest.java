package com.visible.thread.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.event.annotation.AfterTestMethod;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 *
 * This test requires:
 * * mongodb instance running on the environment
 *
 */
@DataMongoTest
public class GridFSLiveTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReactiveGridFsTemplate gridFsTemplate;

    @AfterTestMethod
    public void tearDown() {

    }


    @Test
    public void givenFileWithMetadataExist_whenFindingFileById_thenFileWithMetadataIsFound() {
        DBObject metaData = new BasicDBObject();
        metaData.put("user", "alex");
        InputStream inputStream = null;
        ObjectId id = null;
        try {
            inputStream = new FileInputStream("src/main/resources/test.png");
           // id = gridFsTemplate.store(inputStream, "test.png", "image/png", metaData);
        } catch (FileNotFoundException ex) {
            logger.error("File not found", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Failed to close", ex);
                }
            }
        }

       // GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
/*
        assertThat(gridFSFile).isNotNull();
//        assertNotNull(gridFSFile.getInputStream());
//        assertThat(gridFSFile.numChunks(), is(1));
//        assertThat(gridFSFile.containsField("filename"), is(true));
        assertThat(gridFSFile.getFilename()).isEqualTo("test.png");
        assertThat(gridFSFile.getObjectId()).isEqualTo(id);
//        assertThat(gridFSFile.keySet().size(), is(9));
//        assertNotNull(gridFSFile.getMD5());
        assertThat(gridFSFile.getUploadDate());
//        assertNull(gridFSFile.getAliases());
        assertThat(gridFSFile.getChunkSize()).isNotNull();
        //assertThat(gridFSFile.getMetadata().get("_contentType"), is("image/png"));
        assertThat(gridFSFile.getFilename()).isEqualTo("test.png");
        assertThat(gridFSFile.getMetadata().get("user")).isEqualTo("alex");

 */
    }

    @Test
    public void filterForHighestValuesTestWithReduce() {

        Mono<Integer> numbers = Flux.just(1, 5, 7, 2, 8, 3, 8, 4, 3)
                .reduce( (a,b) ->  { if( a > b) { return a; } else { return b; } });

        StepVerifier.create(numbers)
                .expectNext(8)
                .verifyComplete();
    }

    @Test
    public void givenMetadataAndFilesExist_whenFindingAllFiles_thenFilesWithMetadataAreFound() {
        DBObject metaDataUser1 = new BasicDBObject();
        metaDataUser1.put("user", "alex");
        DBObject metaDataUser2 = new BasicDBObject();
        metaDataUser2.put("user", "david");
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("src/main/resources/test.png");
            //gridFsTemplate.store(inputStream, "test.png", "image/png", metaDataUser1);
           // gridFsTemplate.store(inputStream, "test.png", "image/png", metaDataUser2);
        } catch (FileNotFoundException ex) {
            logger.error("File not found", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Failed to close", ex);
                }
            }
        }

        List<GridFSFile> gridFSFiles = new ArrayList<GridFSFile>();
       // gridFsTemplate.find(new Query()).into(gridFSFiles);

        //assertThat(gridFSFiles).isNotNull();
        //assertThat(gridFSFiles.size()).isEqualTo(2);
    }

    @Test
    public void givenMetadataAndFilesExist_whenFindingAllFilesOnQuery_thenFilesWithMetadataAreFoundOnQuery() {
        DBObject metaDataUser1 = new BasicDBObject();
        metaDataUser1.put("user", "alex");
        DBObject metaDataUser2 = new BasicDBObject();
        metaDataUser2.put("user", "david");
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("src/main/resources/test.png");
            //gridFsTemplate.store(inputStream, "test.png", "image/png", metaDataUser1);
            //gridFsTemplate.store(inputStream, "test.png", "image/png", metaDataUser2);
        } catch (FileNotFoundException ex) {
            logger.error("File not found", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Failed to close", ex);
                }
            }
        }

        List<GridFSFile> gridFSFiles = new ArrayList<GridFSFile>();
       // gridFsTemplate.find(new Query(Criteria.where("metadata.user").is("alex"))).into(gridFSFiles);

       // assertThat(gridFSFiles).isNotNull();
        //assertThat(gridFSFiles.size()).isEqualTo(1);
    }

    @Test
    public void normal() {


        Mono<SortedSet> wordFrequencyMono = Flux.just("the", "quick", "red", "fox", "the", "red", "the")
                .collect(TreeMap<String, Integer>::new, (a, b) -> {
                    if (!a.containsKey(b)) {
                        a.put(b, 1);
                    } else {
                        a.replace(b, a.get(b) + 1);
                    }
                }).map(this::entriesSortedByValuesReverseOrder);

        Flux<Map.Entry<String, Integer>> flux = wordFrequencyMono.flatMapMany(Flux::fromIterable).map(x -> x).take(2);
        flux.subscribe(System.out::println);
        //Mono<Map<String, Integer>> wordFrequencyMonoSorted = wordFrequencyMono.map(x -> this.sortByValue(x));

        wordFrequencyMono.subscribe(System.out::println);

    }

    @Test
    @DisplayName("Collect word with collect operator")
    public void collectMeTreeMap() {

        Mono<TreeMap<String, Long>> collectMe = Flux.just("A", "A", "A", "B")
                .collect(TreeMap<String, Long>::new, (a, b) -> {
                    if (!a.containsKey(b)) {
                        a.put(b, 1L);
                    } else {
                        a.replace(b, a.get(b) + 1L);
                    }
                })
                .doOnNext(e -> System.out.println("Item "+ e));

        Map<String, Long> map = new ConcurrentHashMap<>();
        map.put("A",3L);
        map.put("B",1L);

        final Predicate<Map<String, Long>> predicate = e ->
                map.get("A").equals(e.get("A")) && map.get("B").equals(e.get("B"));

        StepVerifier.create(collectMe)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    @DisplayName("Filter stop words with filter operator")
    public void filterMe() {

        List<String> stopWords = new ArrayList<>();
        stopWords.add("the");
        stopWords.add("B");

        Flux<String> filterMe = Flux.just("A", "A", "the", "B")
                .filter( word -> !stopWords.contains(word))
                .doOnNext(e -> System.out.println("Item "+ e));

        Map<String, Long> map = new ConcurrentHashMap<>();
        map.put("A",3L);
        map.put("B",1L);

        StepVerifier.create(filterMe)
                .expectNext("A")
                .expectNext("A")
                .verifyComplete();
    }

    @Test
    @DisplayName("Collect word with collect operator")
    public void collectMe() {

        Mono<Map<String, Long>> collectMe = Flux.just("A", "A", "A", "B")
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .doOnNext(e -> System.out.println("Item "+ e));

        Map<String, Long> map = new ConcurrentHashMap<>();
        map.put("A",3L);
        map.put("B",1L);

        final Predicate<Map<String, Long>> predicate = e ->
                map.get("A").equals(e.get("A")) && map.get("B").equals(e.get("B"));

        StepVerifier.create(collectMe)
                .expectNextMatches(predicate)
                .verifyComplete();
    }


    private  <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValuesReverseOrder(Map<K,V> map) {
            SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                    new Comparator<Map.Entry<K,V>>() {
                        @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                            int res = e2.getValue().compareTo(e1.getValue());
                            return res != 0 ? res : 1;
                        }
                    }
            );
            sortedEntries.addAll(map.entrySet());
            return sortedEntries;
    }





    @Test
    public void givenFileWithMetadataExist_whenDeletingFileById_thenFileWithMetadataIsDeleted() {
        DBObject metaData = new BasicDBObject();
        metaData.put("user", "alex");
        Path path = FileSystems.getDefault().getPath("src/main/resources/alice_in_wonderland.txt");

        Flux<String> linesIncludingBlanks = fromPath(path);
/*
        FilePart part = new FilePart() {
            @Override
            public String filename() {
                return null;
            }

            @Override
            public Mono<Void> transferTo(Path dest) {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public Flux<DataBuffer> content() {
                return null;
            }
        }

        Mono<FilePart> fileParts = Mono.just(filePart);

        gridFsTemplate.store(linesIncludingBlanks., file.getName(), file.getContentType(), metaData).subscribe();
        // gridFsTemplate.store(dataBufferFlux, "test.png", "image/png", metaData).map((recordId) -> Map.of("id", recordId.toHexString()));
        // id = gridFsTemplate.store(inputStream, "test.png", "image/png", metaData);
        //gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
        //assertThat(gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)))).isNotNull();

        /*
        Flux<String> lines = linesIncludingBlanks.filter(it -> StringUtils.isNotBlank(it));

        Flux<String> wordsFlux = lines.flatMapIterable(this::extractWords);

        Mono<SortedSet> wordFrequencyMono = wordsFlux.collect(TreeMap<String, Integer>::new, (a, b) -> {
            if (!a.containsKey(b)) {
                a.put(b, 1);
            } else {
                a.replace(b, a.get(b) + 1);
            }
        }).map(this::entriesSortedByValuesReverseOrder);//.map(sortedSet -> sortedSet.tailSet());

        wordFrequencyMono.subscribe(System.out::println);

       // Map<String, Integer> wordMap = new HashMap();
       // wordsFlux.collectMap(item -> {return item;}, item -> {

        //  return 0;
       // } );
        //wordsFlux.collectMap(v -> "(" + v +")", v -> v + 10, TreeMap::new)
        //        .subscribe(System.out::println);


        Flux<Pair<String, Long>> wordGroupsFlux = wordsFlux.groupBy(it -> it)
                .flatMap(group -> group.count().map(count -> Pair.of(group.key(), count)));
        wordGroupsFlux.subscribe(System.out::println);

        //Mono<Long> groupCount = wordGroupsFlux.count();
        //groupCount.subscribe( gc -> System.out.println("groupCount=" + gc));
/*
        Flux<List<String>> list = wordGroupsFlux.flatMap(idFlux -> idFlux.collectList().map(listOfWords -> {
            System.out.println("hello "+ listOfWords);
                            return listOfWords;
                        }
                )
        );
        System.out.println("List is ");
           list.subscribe(System.out::println);
*/

        //wordsFlux.subscribe(System.out::println);
/*
        Mono<Long> wordCount = wordsFlux.count();

        wordCount.subscribe(System.out::println);


        Mono<Long> numberOfLines = lines.count();

        Mono<Map<String, Integer>> wordFrequencyMapFlux = lines.collectMap(
        				item -> item.split(" ")[0] ,
        				item -> 0
               );

       // wordFrequencyMapFlux.subscribe(System.out::println);

        Mono<Integer> totalWordCountMono1  = lines.filter(s -> s.contains("Alice"))
                .map(StringTokenizer::new)

                .map(StringTokenizer::countTokens)

                .reduce(0, (x1, x2) -> x1 + x2);

        Mono<Integer> totalWordCountMono  = lines.map(line -> {
            return new StringTokenizer(line).countTokens();
        }).reduce(0, (x1, x2) -> x1 + x2);

        totalWordCountMono1.subscribe(System.out::println);

        Mono<Long> numberOfLinesIncBlanks = linesIncludingBlanks.count();
        numberOfLinesIncBlanks.subscribe(System.out::println);
        numberOfLines.subscribe(System.out::println);
        */


        /*

        // gridFsTemplate.store(file.get, file.getName(), file.getContentType(), metaData).subscribe();
        // gridFsTemplate.store(dataBufferFlux, "test.png", "image/png", metaData).map((recordId) -> Map.of("id", recordId.toHexString()));
        // id = gridFsTemplate.store(inputStream, "test.png", "image/png", metaData);
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
        assertThat(gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)))).isNotNull();

        */

    }

    private <R> List<String> extractWords(String s) {


        List<String> list = new ArrayList();
        StringTokenizer tokenizer =  new StringTokenizer(s);
        while(tokenizer.hasMoreElements()) {
            list.add(tokenizer.nextToken());
        }

        return list;
    }

    @Test
    public void givenFileWithMetadataExist_whenGettingFileByResource_thenFileWithMetadataIsGotten() {
        DBObject metaData = new BasicDBObject();
        metaData.put("user", "alex");
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("src/main/resources/test.png");
            //gridFsTemplate.store(inputStream, "test.png", "image/png", metaData).toString();
        } catch (FileNotFoundException ex) {
            logger.error("File not found", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Failed to close", ex);
                }
            }
        }

       // GridFsResource[] gridFsResource = gridFsTemplate.getResources("test*");

       // assertThat(gridFsResource).isNotNull();
        //assertThat(gridFsResource.length).isEqualTo(1);
       // assertThat(gridFsResource[0].getFilename()).isEqualTo("test.png");
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fileParts", new ClassPathResource("/alice_in_wonderland.txt", GridFSLiveTest.class));
        return builder.build();
    }

    private static Flux<String> fluxVersion(Path path) {
        final Runtime runtime = Runtime.getRuntime();

        return fromPath(path)
                .filter(s -> s.startsWith("Title: ") || s.startsWith("Author: ")
                        || s.equalsIgnoreCase("##BOOKSHELF##"))
                .map(s -> s.replaceFirst("Title: ", ""))
                .map(s -> s.replaceFirst("Author: ", " by "))
                .windowWhile(s -> !s.contains("##"))
                .flatMap(bookshelf -> bookshelf
                        .window(2)
                        .flatMap(bookInfo -> bookInfo.reduce(String::concat))
                        .collectList()
                        .doOnNext(s -> System.gc())
                        .flatMapMany(bookList -> Flux.just(
                                "\n\nFound new Bookshelf of " + bookList.size() + " books:",
                                bookList.toString(),
                                String.format("Memory in use while reading: %dMB\n", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024))
                        )));
    }

    private static Flux<String> fromPath(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }
}
