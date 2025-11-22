package vn.hcmute.edu.authservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.authservice.model.Role;


import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role,String> {
    Optional<Role> findByName(String name);

    // used to prevent deleting a permission still referenced by any role
    boolean existsByPermissionsContains(String permissionName);
}