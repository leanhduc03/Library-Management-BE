package com.spring.LibraryManagement.Repository;

import com.spring.LibraryManagement.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndEmailAndPurposeAndUsedFalse(String token, String email, String purpose);
    Optional<VerificationToken> findByEmailAndPurpose(String email, String purpose);
}