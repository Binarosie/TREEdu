package vn.hcmute.edu.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.userservice.model.AdminEntity;

import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, UUID>, JpaSpecificationExecutor<AdminEntity> {
    boolean existsByEmail(String email);
}