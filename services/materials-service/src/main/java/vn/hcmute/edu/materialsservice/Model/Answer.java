package vn.hcmute.edu.materialsservice.Model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    @Builder.Default
    private String answerId = UUID.randomUUID().toString();

    private String content;

    private Boolean isCorrect;
}
