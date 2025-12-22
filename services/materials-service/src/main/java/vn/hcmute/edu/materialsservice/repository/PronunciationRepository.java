package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.PronunciationHistory;

@Repository
public interface PronunciationRepository extends MongoRepository<PronunciationHistory, String> {
}
