package com.tableorder.repository;

import com.tableorder.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByStoreIdAndUsername(Long storeId, String username);
}
