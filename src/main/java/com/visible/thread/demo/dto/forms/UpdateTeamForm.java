package com.visible.thread.demo.dto.forms;

import lombok.*;

import java.io.Serializable;

/**
 * A form to represent an update to a Team
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class UpdateTeamForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String organisationId;

    private String name;

    private String description;

}

