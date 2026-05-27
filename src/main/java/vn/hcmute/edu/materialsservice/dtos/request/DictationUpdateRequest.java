package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.Data;
import vn.hcmute.edu.materialsservice.models.AudioSegment;
import java.util.List;

@Data
public class DictationUpdateRequest {
    private String title;
    private String level;
    private List<AudioSegment> segments; // Cho phép sửa toàn bộ mảng segments (chữ, thời gian)
}
