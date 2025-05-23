package com.example.jammoney.user.repository;

import com.example.jammoney.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  @EntityGraph(attributePaths = "cash")
  @Query("SELECT u FROM User u JOIN FETCH u.cash")
  List<User> findAllWithCash();
}