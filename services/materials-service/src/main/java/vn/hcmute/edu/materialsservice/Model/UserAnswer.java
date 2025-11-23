package vn.hcmute.edu.materialsservice.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    private String questionId;
    private String selectedAnswerId;
    private Boolean correct;
}
