package com.tableorder.config;

import com.tableorder.entity.Admin;
import com.tableorder.entity.StoreTable;
import com.tableorder.enums.AdminRole;
import com.tableorder.repository.AdminRepository;
import com.tableorder.repository.StoreTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 로컬 개발 환경에서 시드 데이터의 bcrypt 해시를 올바르게 업데이트합니다.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final StoreTableRepository storeTableRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String rawPassword = "password1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 관리자 비밀번호 업데이트
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            admin.updatePasswordHash(encodedPassword);
            adminRepository.save(admin);
        }
        log.info("관리자 {} 명의 비밀번호를 업데이트했습니다. (password1234)", admins.size());

        // 테이블 비밀번호 업데이트
        List<StoreTable> tables = storeTableRepository.findAll();
        for (StoreTable table : tables) {
            table.updatePasswordHash(encodedPassword);
            storeTableRepository.save(table);
        }
        log.info("테이블 {} 개의 비밀번호를 업데이트했습니다. (password1234)", tables.size());
    }
}
