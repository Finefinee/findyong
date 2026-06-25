package com.example.lostfound_project.repository;

import com.example.lostfound_project.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    @Query("""
            select item
            from LostItem item
            where (:keyword is null
                or lower(item.itemName) like lower(concat('%', :keyword, '%'))
                or lower(item.description) like lower(concat('%', :keyword, '%')))
            and (:location is null
                or lower(item.location) like lower(concat('%', :location, '%')))
            order by item.lostTime desc, item.id desc
            """)
    List<LostItem> searchLostItems(
            @Param("keyword") String keyword,
            @Param("location") String location
    );
}
