package com.spring.LibraryManagement.Service;

import com.spring.LibraryManagement.Entity.Permission;
import com.spring.LibraryManagement.Entity.Role;
import com.spring.LibraryManagement.Entity.User;
import com.spring.LibraryManagement.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        log.info("Username: {}", user.getUsername());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Thêm vai trò
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        
        // Thêm tất cả quyền của vai trò
        user.getRole().getPermissions().forEach(permission -> 
            authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission.name()))
        );
        log.info("authorities: {}", authorities);
        return authorities;
    }
    
    public boolean hasPermission(String username, String targetType, Long targetId, String permission) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
            
        // Nếu là ADMIN, luôn cho phép
        if (user.getRole() == Role.ROLE_ADMIN) {
            return true;
        }
        
        // Kiểm tra quyền cụ thể
        Permission requiredPermission = Permission.valueOf(permission);
        if (user.getRole().getPermissions().contains(requiredPermission)) {
            // Với BORROW, người dùng chỉ có thể xem/sửa đổi borrow của chính họ
            if ("BORROW".equals(targetType) && !isUserOwnBorrow(user.getId(), targetId)) {
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    private boolean isUserOwnBorrow(Long userId, Long borrowId) {
        // Triển khai kiểm tra xem borrowId có thuộc về userId không
        // Giả lập: return borrowRepository.findById(borrowId).map(b -> b.getUser().getId().equals(userId)).orElse(false);
        return true; // Giả lập kết quả
    }
}
