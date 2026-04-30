package com.tableorder.enums;

public enum AdminRole {
    OWNER,
    MANAGER,
    STAFF;

    public boolean hasPermission(String permission) {
        return switch (this) {
            case OWNER -> true;
            case MANAGER -> !permission.equals("ADMIN_ACCOUNT_MANAGE");
            case STAFF -> permission.equals("ORDER_MONITOR") || permission.equals("ORDER_STATUS_CHANGE");
        };
    }
}
