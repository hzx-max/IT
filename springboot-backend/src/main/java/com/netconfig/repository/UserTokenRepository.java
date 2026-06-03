package com.netconfig.repository;

import com.netconfig.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    Optional<UserToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}