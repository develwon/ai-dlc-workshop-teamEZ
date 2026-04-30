package com.tableorder.entity;

import com.tableorder.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("PENDING 상태에서 PREPARING으로 전환 가능")
    void testPendingCanTransitionToPreparing() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PREPARING)).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태에서 COMPLETED로 전환 가능")
    void testPendingCanTransitionToCompleted() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("PREPARING 상태에서 COMPLETED로 전환 가능")
    void testPreparingCanTransitionToCompleted() {
        assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("PREPARING 상태에서 PENDING으로 전환 불가")
    void testPreparingCannotTransitionToPending() {
        assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태에서는 어떤 상태로도 전환 불가")
    void testCompletedCannotTransition() {
        assertThat(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PREPARING)).isFalse();
    }
}
