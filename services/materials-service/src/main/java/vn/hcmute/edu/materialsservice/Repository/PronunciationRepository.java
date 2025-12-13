package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Model.PronunciationHistory;

@Repository
public interface PronunciationRepository extends MongoRepository<PronunciationHistory, String> {
}
