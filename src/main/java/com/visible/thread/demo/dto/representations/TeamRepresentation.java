package com.visible.thread.demo.dto.representations;

import lombok.*;

import java.io.Serializable;

/**
 * Team Representation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TeamRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String organisationId;

    private String name;

    private String description;

}

