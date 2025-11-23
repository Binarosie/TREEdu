package vn.hcmute.edu.materialsservice.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AnswerResponse {
    private String answerId;

    private String content;
}
