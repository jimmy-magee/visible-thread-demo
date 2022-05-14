package com.visible.thread.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
@Document(collection = "vtdocs")
public final class VTDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String organisationId;

    private String userId;

    private String userEmail;

    private String teamId;

    private String content;

    private String name;

    private String description;

    private LocalDateTime createdDate;

    private LocalDateTime modificationDate;

}