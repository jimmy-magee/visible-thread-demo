package com.visible.thread.demo.dto.representations;

import lombok.*;

import java.io.Serializable;

/**
 * Organisation Representation
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class OrganisationRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String description;

}

