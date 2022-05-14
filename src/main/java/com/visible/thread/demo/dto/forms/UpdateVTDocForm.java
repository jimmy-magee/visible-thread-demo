package com.visible.thread.demo.dto.forms;

import lombok.*;

import java.io.Serializable;

/**
 * A form to represent an update to a VTDoc
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class UpdateVTDocForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String teamId;
    private String userId;
    private String content;
    private String filename;
    private String contentType;
    private String documentType;

}

