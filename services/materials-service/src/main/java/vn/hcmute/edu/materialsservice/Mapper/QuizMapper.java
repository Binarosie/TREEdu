package vn.hcmute.edu.materialsservice.Mapper;


import org.mapstruct.*;
import vn.hcmute.edu.materialsservice.Dto.request.AnswerRequest;
import vn.hcmute.edu.materialsservice.Dto.request.QuestionRequest;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.AnswerResponse;
import vn.hcmute.edu.materialsservice.Dto.response.QuestionResponse;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponseWithQuestion;
import vn.hcmute.edu.materialsservice.Model.Answer;
import vn.hcmute.edu.materialsservice.Model.Question;
import vn.hcmute.edu.materialsservice.Model.Quiz;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface QuizMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionCount", expression = "java(request.getQuestions().size())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Quiz toEntity(QuizRequest request);

    @Mapping(target = "questionId", expression = "java(java.util.UUID.randomUUID().toString())")
    Question toEntity(QuestionRequest request);

    @Mapping(target = "answerId", expression = "java(java.util.UUID.randomUUID().toString())")
    Answer toEntity(AnswerRequest request);

    // Entity to Response mappings
    QuizResponseWithQuestion toResponse(Quiz quiz);

    QuestionResponse toResponse(Question question);

    @Mapping(target = "answerId", source = "answerId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "isCorrect", source = "isCorrect")
    AnswerResponse toResponse(Answer answer);

    // List mappings
    List<Question> toEntityList(List<QuestionRequest> dtos);

    List<Answer> toAnswerEntityList(List<AnswerRequest> dtos);

    List<QuestionResponse> toQuestionResponseList(List<Question> questions);

    List<AnswerResponse> toAnswerResponseList(List<Answer> answers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "questionCount", expression = "java(request.getQuestions().size())")
    void updateEntityFromDTO(QuizRequest request, @MappingTarget Quiz quiz);
}
