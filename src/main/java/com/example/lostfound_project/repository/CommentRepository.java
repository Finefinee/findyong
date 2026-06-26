package com.example.lostfound_project.repository;

import com.example.lostfound_project.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedAtAscIdAsc(Long itemId);
}
