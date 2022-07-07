package com.visible.thread.demo.dto.forms;

import lombok.*;

import java.io.Serializable;

/**
 * A form to represent an update to a User
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class UpdateUserForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String organisationId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String imageUrl;

}
