package vn.hcmute.edu.materialsservice.Mapper;

import org.mapstruct.*;
import vn.hcmute.edu.materialsservice.dtos.response.TopicDetailResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;
import vn.hcmute.edu.materialsservice.models.Topic;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {

        // Dùng expression tính sentenceCount trực tiếp, tránh @AfterMapping bị bỏ qua
        @Mapping(target = "sentenceCount", expression = "java(topic.getSentences() != null ? topic.getSentences().size() : 0)")
        TopicResponse toResponse(Topic topic);

        // toResponseList dùng lại toResponse ở trên nên sentenceCount tự đúng
        List<TopicResponse> toResponseList(List<Topic> topics);

        @Mapping(target = "sentenceCount", expression = "java(topic.getSentences() != null ? topic.getSentences().size() : 0)")
        TopicDetailResponse toDetailResponse(Topic topic);
}
