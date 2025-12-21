package vn.hcmute.edu.materialsservice.Mapper;

import org.mapstruct.Mapper;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.models.PronunciationHistory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PronunciationMapper {
    PronunciationCheckResponse toResponse(PronunciationHistory history);

    List<PronunciationCheckResponse> toResponseList(List<PronunciationHistory> histories);

    PronunciationCheckResponse.PronunciationError toErrorResponse(PronunciationHistory.PronunciationError error);
}
