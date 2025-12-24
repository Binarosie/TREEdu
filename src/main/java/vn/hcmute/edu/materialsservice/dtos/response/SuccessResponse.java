package vn.hcmute.edu.materialsservice.dtos.response;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private String message;
    private int statusCode;
    private Object data;
    private LocalDateTime timestamp;
}