package com.example.ecommerceapplication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecommerceapplication.model.AdminInvitationToken;

@Repository
public interface AdminTokenRepository extends JpaRepository<AdminInvitationToken, Long> {
    Optional<AdminInvitationToken> findByToken(String token);
    //List<AdminInvitationToken> findAllByUserAndRevokedIsFalseAndExpiredIsFalse(User user);
    void deleteByToken(String token);
    boolean existsByToken(String token);
    //void deleteAllExpiredTokens();
}
