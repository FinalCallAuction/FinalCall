// src/main/java/com/finalcall/backend/repository/ItemRepository.java

package com.finalcall.backend.repository;

import com.finalcall.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findById(Long id);
}
