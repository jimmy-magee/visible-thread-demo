package com.visible.thread.demo.dto.forms;

import lombok.*;

import java.io.Serializable;

/**
 * A form to represent a user creation date range query.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@ToString
public class UserCreationQueryForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String startDate;

    private String endDate;


}
