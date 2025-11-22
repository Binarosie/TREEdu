package vn.hcmute.edu.authservice.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.authservice.model.Permission;


import java.util.Optional;

@Repository
public interface PermissionRepository extends MongoRepository<Permission,String> {
    Optional<Permission> findByName(String name);
}
