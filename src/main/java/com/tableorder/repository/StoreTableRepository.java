package com.tableorder.repository;

import com.tableorder.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {

    Optional<StoreTable> findByStoreIdAndTableNumber(Long storeId, Integer tableNumber);

    Optional<StoreTable> findByIdAndStoreId(Long id, Long storeId);

    List<StoreTable> findAllByStoreId(Long storeId);
}
