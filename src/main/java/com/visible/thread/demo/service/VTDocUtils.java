package com.visible.thread.demo.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class VTDocUtils {

    public static final String REGEX_RULES = "^01\\d{9}$|^1\\d{9}|^d{0}$";

    private static final StringDecoder stringDecoder = StringDecoder.textPlainOnly();

    private static final ResolvableType STRING_TYPE = ResolvableType.forClass(String.class);

    /**
     *
     * @param wordsFlux
     * @return
     */
    public static Mono<Long> calculateWordCount(final Flux<String> wordsFlux) {
        return wordsFlux.count();
    }

    /**
     *
     * @param inputStream
     * @return
     */
    public static Flux<String> dataBufferToStringFlux(Flux<DataBuffer> inputStream) {
        return stringDecoder.decode(inputStream, STRING_TYPE, null, null);
    }

    /**
     *
     * @param wordsFlux
     * @param size
     * @return
     */
    public static Mono<List<Map.Entry<String, Long>>> calculateWordFrequency(final Flux<String> wordsFlux, int size) {
        return wordsFlux.collect(TreeMap<String, Long>::new, (a, b) -> {
                    if (!a.containsKey(b)) {
                        a.put(b, 1L);
                    } else {
                        a.replace(b, a.get(b) + 1L);
                    }
                }).map(VTDocUtils::entriesSortedByValuesReverseOrder)
                .flatMap(set -> Flux.fromIterable(set).take(size).collectList());

    }


    /**
     *
     * @param s
     * @param <R>
     * @return
     */
    public static <R> List<String> extractWords(String s) {

        List<String> list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(s);
        while (tokenizer.hasMoreElements()) {
            list.add(tokenizer.nextToken());
        }
        return list;
    }

    /**
     *
     * @param filePart
     * @return
     */
    public static Flux<String> getLines(FilePart filePart) {
        log.debug("Processing filePart {}", filePart);
        return filePart.content()
                .filter(buffer -> buffer != null)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    //log.debug("Processing string {}", bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                });
    }

    /**
     *
     * @param firstDate
     * @param secondDate
     * @return
     */
    public static Boolean compareDates(LocalDate firstDate, LocalDate secondDate) {

        if(firstDate == null ) {
            return Boolean.FALSE;
        }
        if(secondDate == null ) {
            return  Boolean.FALSE;
        }
        return firstDate.getYear() == secondDate.getYear() &&
                firstDate.getMonth() == secondDate.getMonth() &&
                firstDate.getDayOfMonth() == secondDate.getDayOfMonth();

    }

    /**
     *
     * @param fsResource
     * @return
     */
    public static LocalDate getFileCreatedDate(ReactiveGridFsResource fsResource) {
        if(fsResource.getOptions().getMetadata().containsKey("createdDate")) {
            return fsResource.getOptions().getMetadata()
                    .get("createdDate", Date.class)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else {
            return null;
        }
    }

    /**
     *
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValuesReverseOrder(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    /**
     *
     * @param filePartFlux
     * @return
     */
    // this is for multiple file upload
    public static Flux<String> getLines(Flux<FilePart> filePartFlux) {

        return filePartFlux.flatMap(VTDocUtils::getLines);
    }

    /**
     *
     * @param filePartMono
     * @return
     */
    // this is for single file upload
    public static Flux<String> getLines(Mono<FilePart> filePartMono) {

        return filePartMono.flatMapMany(VTDocUtils::getLines);
    }


}
