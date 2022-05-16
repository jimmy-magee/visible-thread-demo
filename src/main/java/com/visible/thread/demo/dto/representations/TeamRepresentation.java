package com.visible.thread.demo.dto.representations;

import com.visible.thread.demo.model.User;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private List<User> users = new ArrayList<>();

}

