package com.tableorder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_tables", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "table_number"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Builder
    public StoreTable(Long storeId, Integer tableNumber, String passwordHash) {
        this.storeId = storeId;
        this.tableNumber = tableNumber;
        this.passwordHash = passwordHash;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
