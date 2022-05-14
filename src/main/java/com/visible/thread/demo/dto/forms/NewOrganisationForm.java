package com.visible.thread.demo.dto.forms;

import lombok.*;
import java.io.Serializable;

/**
 * A form to represent a new Organisation
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class NewOrganisationForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String description;

}
