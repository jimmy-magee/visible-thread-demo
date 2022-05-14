package com.visible.thread.demo.dto.forms;

import lombok.*;
import java.io.Serializable;

/**
 * A form to represent a new VTDoc
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class NewVTDocForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String organisationId;
    private String teamId;
    private String userId;
    private String content;
    private String filename;
    private String contentType;
    private String documentType;

}
