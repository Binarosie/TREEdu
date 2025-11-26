package vn.hcmute.edu.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.userservice.model.SupporterEntity;
import java.util.UUID;

@Repository
public interface SupporterRepository extends JpaRepository<SupporterEntity, UUID>, JpaSpecificationExecutor<SupporterEntity> {
    boolean existsByEmail(String email);
}