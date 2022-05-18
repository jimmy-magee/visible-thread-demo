package com.visible.thread.demo;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.event.annotation.AfterTestMethod;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
    public void filterForHighestValuesTestWithReduce() {

        Mono<Integer> numbers = Flux.just(1, 5, 7, 2, 8, 3, 8, 4, 3)
                .reduce( (a,b) ->  { if( a > b) { return a; } else { return b; } });

        StepVerifier.create(numbers)
                .expectNext(8)
                .verifyComplete();
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

    public static <R> List<String> extractWords(String s) {

        List<String> list = new ArrayList();
        StringTokenizer tokenizer =  new StringTokenizer(s);
        while(tokenizer.hasMoreElements()) {
            list.add(tokenizer.nextToken());
        }

        return list;
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
