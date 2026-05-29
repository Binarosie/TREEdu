package vn.hcmute.edu.materialsservice.Mapper;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.dtos.response.DictationCheckResponse;

import java.util.List;

@Component
public class DictationMapper {

    public DictationCheckResponse toCheckResponse(Double accuracy, Boolean passed,
                                                  String correctAnswer,
                                                  List<DictationCheckResponse.WordDiff> wordDetails) {
        return DictationCheckResponse.builder()
                .accuracy(accuracy)
                .passed(passed)
                .correctAnswer(correctAnswer)
                .wordDetails(wordDetails)
                .build();
    }
}
