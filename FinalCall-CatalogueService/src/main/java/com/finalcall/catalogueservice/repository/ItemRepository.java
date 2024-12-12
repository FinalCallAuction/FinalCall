// src/main/java/com/finalcall/catalogueservice/repository/ItemRepository.java

package com.finalcall.catalogueservice.repository;

import com.finalcall.catalogueservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByRandomId(String randomId);
    List<Item> findByListedBy(Long listedBy);
}


