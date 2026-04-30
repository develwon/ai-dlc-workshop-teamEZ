package com.tableorder.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNumberGeneratorTest {

    private final OrderNumberGenerator orderNumberGenerator = new OrderNumberGenerator();

    @Test
    @DisplayName("주문번호 생성 시 null이 아님")
    void testGenerateReturnsNonNull() {
        String orderNumber = orderNumberGenerator.generate();

        assertThat(orderNumber).isNotNull();
    }

    @Test
    @DisplayName("주문번호 형식 검증 (yyyyMMdd-HHmmss-XXXX)")
    void testGenerateFormat() {
        String orderNumber = orderNumberGenerator.generate();

        assertThat(orderNumber).matches("\\d{8}-\\d{6}-[A-Z0-9]{4}");
    }

    @Test
    @DisplayName("주문번호 100개 생성 시 모두 고유")
    void testGenerateUniqueness() {
        Set<String> orderNumbers = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            orderNumbers.add(orderNumberGenerator.generate());
        }

        assertThat(orderNumbers).hasSize(100);
    }
}
