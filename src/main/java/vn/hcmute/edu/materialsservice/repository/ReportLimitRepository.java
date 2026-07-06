package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.ReportLimit;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReportLimitRepository extends MongoRepository<ReportLimit, String> {

    Optional<ReportLimit> findByMemberId(String memberId);

    Optional<ReportLimit> findByMemberIdAndResetDateGreaterThanEqual(String memberId, LocalDate today);
}
