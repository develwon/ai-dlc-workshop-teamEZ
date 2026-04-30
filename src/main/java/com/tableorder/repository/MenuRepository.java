package com.tableorder.repository;

import com.tableorder.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStoreIdOrderByDisplayOrder(Long storeId);

    List<Menu> findByStoreIdAndCategoryIdOrderByDisplayOrder(Long storeId, Long categoryId);

    List<Menu> findByStoreIdOrderByDisplayOrderAsc(Long storeId);

    List<Menu> findByStoreIdAndCategoryIdOrderByDisplayOrderAsc(Long storeId, Long categoryId);

    Optional<Menu> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByCategoryId(Long categoryId);
}
