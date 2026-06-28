package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AudioSegment {
    private Integer id;
    private Double startTime;
    private Double endTime;
    private String transcript;
}
