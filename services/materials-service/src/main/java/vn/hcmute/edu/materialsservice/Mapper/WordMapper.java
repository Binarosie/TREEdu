package vn.hcmute.edu.materialsservice.Mapper;

import org.mapstruct.Mapper;
import vn.hcmute.edu.materialsservice.dtos.request.WordRequest;
import vn.hcmute.edu.materialsservice.dtos.response.WordResponse;
import vn.hcmute.edu.materialsservice.models.Word;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WordMapper {

    Word toEntity(WordRequest request);

    WordResponse toResponse(Word word);

    List<WordResponse> toResponseList(List<Word> words);
}
