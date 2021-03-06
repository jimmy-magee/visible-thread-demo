package com.visible.thread.demo.model;

import lombok.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
@Document(collection = "users")
public final class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String email;

    private Boolean isEmailVerified;

    private String organisationId;

    private String firstname;

    private String lastname;

    private String phone;

    private String imageId;

    private byte[] image;

    private LocalDateTime createdDate;

    private LocalDateTime modificationDate;

}
