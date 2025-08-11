package com.spring.LibraryManagement.Entity;

import java.util.Collections;
import java.util.Set;

public enum Role {
    ROLE_USER(Set.of(
            Permission.BOOK_READ,
            Permission.BORROW_READ,
            Permission.BORROW_CREATE,
            Permission.BORROW_UPDATE,
            Permission.FINE_READ)),

    ROLE_ADMIN(Set.of(
            Permission.BOOK_READ,
            Permission.BOOK_CREATE,
            Permission.BOOK_UPDATE,
            Permission.BOOK_DELETE,
            Permission.USER_READ,
            Permission.USER_CREATE,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,
            Permission.BORROW_READ,
            Permission.BORROW_CREATE,
            Permission.BORROW_UPDATE,
            Permission.BORROW_DELETE,
            Permission.FINE_READ,
            Permission.FINE_UPDATE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
