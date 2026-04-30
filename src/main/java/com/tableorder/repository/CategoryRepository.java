package com.tableorder.repository;

import com.tableorder.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStoreIdOrderByDisplayOrder(Long storeId);

    Optional<Category> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);

    boolean existsByStoreIdAndNameAndIdNot(Long storeId, String name, Long id);
}
