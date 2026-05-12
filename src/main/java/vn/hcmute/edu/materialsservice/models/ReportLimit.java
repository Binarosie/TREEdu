package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "report_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportLimit {
    @Id
    private String id;

    private String memberId; // Member ID
    private Integer reportsLeft; // Số lượt report còn lại trong ngày
    private LocalDate resetDate; // Ngày reset tiếp theo
}
