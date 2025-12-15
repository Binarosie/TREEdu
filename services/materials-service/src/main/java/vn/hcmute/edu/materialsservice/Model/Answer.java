package vn.hcmute.edu.materialsservice.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore // Ẩn isCorrect khi serialize JSON để không lộ đáp án
    private Boolean isCorrect;
}
