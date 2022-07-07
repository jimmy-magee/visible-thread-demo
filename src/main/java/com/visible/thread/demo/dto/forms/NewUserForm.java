package com.visible.thread.demo.dto.forms;

import lombok.*;
import java.io.Serializable;

/**
 * A form to represent a new User
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class NewUserForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String organisationId;
    private String teamId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String imageUrl;

}
