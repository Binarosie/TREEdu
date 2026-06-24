package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentencesRequest {
    private List<String> sentences;
}
