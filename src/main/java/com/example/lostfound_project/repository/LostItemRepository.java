package com.example.lostfound_project.repository;

import com.example.lostfound_project.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
}
