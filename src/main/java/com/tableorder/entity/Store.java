package com.tableorder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_code", length = 50, nullable = false, unique = true)
    private String storeCode;

    @Column(name = "store_name", length = 100, nullable = false)
    private String storeName;

    @Builder
    public Store(String storeCode, String storeName) {
        this.storeCode = storeCode;
        this.storeName = storeName;
    }

    public void updateStoreName(String storeName) {
        this.storeName = storeName;
    }
}
