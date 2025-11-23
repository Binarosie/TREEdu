package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hcmute.edu.materialsservice.Model.QuizAttempt;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
}
