package com.visible.thread.demo.dto.representations;

import lombok.*;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * User Representation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String organisationId;

    private String firstname;

    private String lastname;

    private Boolean isEmailVerified;

    private String email;

    private String imageId;

    private byte[] image;

}
