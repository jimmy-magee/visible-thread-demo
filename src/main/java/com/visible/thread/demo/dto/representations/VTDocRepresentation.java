package com.visible.thread.demo.dto.representations;

import lombok.*;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * User Representation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class VTDocRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String organisationId;

    private String teamId;

    private String userId;

    private String fileName;

    private Long wordCount;

    private List wordFrequency;

    private String dateUploaded;

}
