package vn.hcmute.edu.materialsservice.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.request.WordRequest;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;
import vn.hcmute.edu.materialsservice.Model.Flashcard;
import vn.hcmute.edu.materialsservice.Model.Word;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlashcardMapper {

    Flashcard toEntity(FlashcardRequest request);

    @Mapping(target = "type", expression = "java(flashcard.getType() != null ? flashcard.getType().name() : null)")
    @Mapping(target = "isOwner", ignore = true)
    FlashcardResponse toResponse(Flashcard flashcard);

    List<FlashcardResponse> toResponseList(List<Flashcard> flashcards);

    Word toWordEntity(WordRequest request);

    WordResponse toWordResponse(Word word);
}
