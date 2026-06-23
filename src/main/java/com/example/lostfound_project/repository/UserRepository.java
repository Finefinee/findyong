package com.example.lostfound_project.repository;

import com.example.lostfound_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
    User findByUserId(String userId);
}
