package org.example.avitotech.model;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AssignedReviewerId implements Serializable {

    private String pullRequestId;
    private String userId;
}
