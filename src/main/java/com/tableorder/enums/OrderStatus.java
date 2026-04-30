package com.tableorder.enums;

public enum OrderStatus {
    PENDING,
    PREPARING,
    COMPLETED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PREPARING || target == COMPLETED;
            case PREPARING -> target == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
