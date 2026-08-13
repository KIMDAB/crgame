package com.toy.cnr.rds.user;

import com.toy.cnr.rds.user.entity.UserAuthLocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAuthLocalJpaRepository extends JpaRepository<UserAuthLocalEntity, Long> {

    @Query("SELECT a.passwordHash FROM UserAuthLocalEntity a JOIN UserEntity u ON a.userId = u.id WHERE u.email = :email")
    Optional<String> findPasswordHashByEmail(@Param("email") String email);
}
