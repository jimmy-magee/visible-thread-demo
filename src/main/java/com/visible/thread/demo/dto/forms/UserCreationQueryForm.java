package com.visible.thread.demo.dto.forms;


        import java.time.LocalDateTime;
        import lombok.*;

        import java.io.Serializable;

/**
 * A form to represent a user creation date range query.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class UserCreationQueryForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
