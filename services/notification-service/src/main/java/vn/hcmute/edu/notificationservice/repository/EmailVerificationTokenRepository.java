package vn.hcmute.edu.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.notificationservice.model.EmailVerificationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserIdAndVerifiedFalse(String userId);

    List<EmailVerificationToken> findByEmailAndVerifiedFalse(String email);

    void deleteByUserId(String userId);

    void deleteByExpiresAtBefore(Instant expiredBefore);

    boolean existsByTokenAndVerifiedTrue(String token);
}