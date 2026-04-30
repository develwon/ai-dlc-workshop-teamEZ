package com.tableorder.service;

import com.tableorder.dto.CreateTableRequest;
import com.tableorder.dto.TableResponse;
import com.tableorder.dto.TableSummaryResponse;
import com.tableorder.entity.StoreTable;
import com.tableorder.repository.StoreTableRepository;
import com.tableorder.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTableService {

    private final StoreTableRepository storeTableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TableResponse createOrUpdateTable(Long storeId, CreateTableRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        StoreTable table = storeTableRepository
                .findByStoreIdAndTableNumber(storeId, request.getTableNumber())
                .map(existing -> {
                    existing.updatePassword(encodedPassword);
                    log.info("Table password updated [storeId={}, tableNumber={}]",
                            storeId, request.getTableNumber());
                    return existing;
                })
                .orElseGet(() -> {
                    StoreTable newTable = StoreTable.builder()
                            .storeId(storeId)
                            .tableNumber(request.getTableNumber())
                            .passwordHash(encodedPassword)
                            .build();
                    log.info("New table created [storeId={}, tableNumber={}]",
                            storeId, request.getTableNumber());
                    return newTable;
                });

        StoreTable saved = storeTableRepository.save(table);

        return TableResponse.builder()
                .id(saved.getId())
                .storeId(saved.getStoreId())
                .tableNumber(saved.getTableNumber())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TableSummaryResponse> getTables(Long storeId) {
        List<StoreTable> tables = storeTableRepository.findAllByStoreIdOrderByTableNumberAsc(storeId);

        return tables.stream()
                .map(table -> {
                    boolean hasActiveSession = tableSessionRepository
                            .findByTableIdAndEndTimeIsNull(table.getId())
                            .isPresent();

                    return TableSummaryResponse.builder()
                            .tableId(table.getId())
                            .tableNumber(table.getTableNumber())
                            .hasActiveSession(hasActiveSession)
                            .build();
                })
                .toList();
    }
}
