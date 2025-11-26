package vn.hcmute.edu.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.userservice.model.MemberEntity;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, UUID>, JpaSpecificationExecutor<MemberEntity> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}