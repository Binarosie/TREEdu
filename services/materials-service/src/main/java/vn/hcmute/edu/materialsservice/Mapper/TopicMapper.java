package vn.hcmute.edu.materialsservice.Mapper;
import org.mapstruct.*;
import vn.hcmute.edu.materialsservice.Dto.response.TopicResponse;
import vn.hcmute.edu.materialsservice.Model.Topic;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    List<TopicResponse> toResponseList(List<Topic> topics);

    TopicResponse toResponse(Topic topic);

    @AfterMapping
    default void setSentenceCount(@MappingTarget TopicResponse response, Topic topic) {
        response.setSentenceCount(
                topic.getSentences() != null ? topic.getSentences().size() : 0
        );
    }
}
