package com.example.jammoney.user.repository;

import com.example.jammoney.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);

  @EntityGraph(attributePaths = "cash")
  @Query("select u from User u")
  Page<User> findAllWithCash(Pageable pageable);
}
