package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.Topic;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    Topic findByName(String name);

}
