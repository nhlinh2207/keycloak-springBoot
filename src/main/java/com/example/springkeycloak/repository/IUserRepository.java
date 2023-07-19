package com.example.springkeycloak.repository;

import com.example.springkeycloak.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {

    User findByVerificationCode(String verificationCode);
    User findByEmail(String email);

    User findByUsername(String username);
}
