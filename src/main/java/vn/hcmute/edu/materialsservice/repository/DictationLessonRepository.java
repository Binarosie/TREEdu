package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Enum.ELessonStatus;
import vn.hcmute.edu.materialsservice.models.DictationLesson;

import java.util.List;

@Repository
public interface DictationLessonRepository extends MongoRepository<DictationLesson, String> {
    List<DictationLesson> findAllByStatus(ELessonStatus status);
}
