package com.visible.thread.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
@Document(collection = "teams")
public final class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String organisationId;

    private String name;

    private String description;

    @Builder.Default
    private Set<String> users = new HashSet<>();

    private LocalDateTime createdDate;

    private LocalDateTime modificationDate;

}
