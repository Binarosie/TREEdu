package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Model.Topic;

import java.util.List;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    Topic findByName(String name);

}
