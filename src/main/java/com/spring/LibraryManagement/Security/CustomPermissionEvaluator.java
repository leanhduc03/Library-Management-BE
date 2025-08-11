package com.spring.LibraryManagement.Security;

import com.spring.LibraryManagement.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserService userService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        
        String username = authentication.getName();
        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
        
        return userService.hasPermission(username, targetType, null, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || !(permission instanceof String)) {
            return false;
        }
        
        String username = authentication.getName();
        Long id = targetId instanceof Long ? (Long) targetId : Long.valueOf(targetId.toString());
        
        return userService.hasPermission(username, targetType, id, permission.toString());
    }
}