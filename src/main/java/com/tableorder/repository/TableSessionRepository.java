package com.tableorder.repository;

import com.tableorder.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, Long> {

    Optional<TableSession> findByTableIdAndEndTimeIsNull(Long tableId);

    List<TableSession> findByTableIdOrderByStartTimeDesc(Long tableId);
}
