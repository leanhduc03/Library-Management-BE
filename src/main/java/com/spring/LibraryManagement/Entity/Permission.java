package com.spring.LibraryManagement.Entity;

public enum Permission {
    // Quyền quản lý sách
    BOOK_READ,
    BOOK_CREATE,
    BOOK_UPDATE,
    BOOK_DELETE,
    
    // Quyền quản lý người dùng
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    
    // Quyền mượn sách
    BORROW_READ,
    BORROW_CREATE,
    BORROW_UPDATE,
    BORROW_DELETE,

    FINE_READ,
    FINE_UPDATE
}